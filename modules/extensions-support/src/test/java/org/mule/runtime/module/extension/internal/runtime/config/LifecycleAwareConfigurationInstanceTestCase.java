/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mule.runtime.api.connection.ConnectionExceptionCode.UNKNOWN;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_CONNECTION_MANAGER;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_TIME_SUPPLIER;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.lifecycle.Disposable;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.Lifecycle;
import org.mule.runtime.core.api.lifecycle.Startable;
import org.mule.runtime.core.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.retry.RetryNotifier;
import org.mule.runtime.core.api.retry.RetryPolicyTemplate;
import org.mule.runtime.core.internal.connection.ConnectionManagerAdapter;
import org.mule.runtime.core.internal.metadata.MuleMetadataManager;
import org.mule.runtime.core.retry.RetryPolicyExhaustedException;
import org.mule.runtime.core.retry.policies.SimpleRetryPolicyTemplate;
import org.mule.runtime.extension.api.runtime.ConfigurationInstance;
import org.mule.runtime.module.extension.internal.AbstractInterceptableContractTestCase;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.size.SmallTest;
import org.mule.tck.util.TestTimeSupplier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

@SmallTest
@RunWith(Parameterized.class)
public class LifecycleAwareConfigurationInstanceTestCase
    extends AbstractInterceptableContractTestCase<LifecycleAwareConfigurationInstance> {

  private static final int RECONNECTION_MAX_ATTEMPTS = 5;
  private static final int RECONNECTION_FREQ = 100;
  private static final String NAME = "name";

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
                         new Object[][] {
                             {"With provider",
                                 mock(ConnectionProvider.class,
                                      withSettings().extraInterfaces(Lifecycle.class, MuleContextAware.class))},
                             {"Without provider", null}});
  }

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private ConfigurationModel configurationModel;

  @Mock
  private Lifecycle value;

  @Mock
  private ConnectionManagerAdapter connectionManager;

  private RetryPolicyTemplate retryPolicyTemplate;

  private String name;
  private Optional<ConnectionProvider> connectionProvider;

  public LifecycleAwareConfigurationInstanceTestCase(String name, ConnectionProvider connectionProvider) {
    this.name = name;
    this.connectionProvider = Optional.ofNullable(connectionProvider);
  }

  private TestTimeSupplier timeSupplier = new TestTimeSupplier(System.currentTimeMillis());

  @Before
  @Override
  public void before() throws Exception {
    initMocks(this);
    muleContext.getRegistry().registerObject(OBJECT_CONNECTION_MANAGER, connectionManager);
    muleContext.getRegistry().registerObject(OBJECT_TIME_SUPPLIER, timeSupplier);

    retryPolicyTemplate = new SimpleRetryPolicyTemplate(RECONNECTION_FREQ, RECONNECTION_MAX_ATTEMPTS);
    retryPolicyTemplate.setNotifier(mock(RetryNotifier.class));

    super.before();
  }

  @Override
  protected LifecycleAwareConfigurationInstance createInterceptable() {
    if (connectionProvider.isPresent()) {
      reset(connectionProvider.get());
    }
    setup(connectionManager);
    return new LifecycleAwareConfigurationInstance(NAME, configurationModel, value, getInterceptors(), connectionProvider);
  }

  private void setup(ConnectionManagerAdapter connectionManager) {
    if (connectionProvider.isPresent()) {
      when(connectionManager.getRetryTemplateFor(connectionProvider.get())).thenReturn(retryPolicyTemplate);
      when(connectionManager.testConnectivity(Mockito.any(ConfigurationInstance.class))).thenReturn(success());
    }
  }

  private void reset(Object object) {
    Mockito.reset(object);
    if (object instanceof ConnectionManagerAdapter) {
      setup((ConnectionManagerAdapter) object);
    }
  }

  @Test
  public void valueInjected() throws Exception {
    interceptable.initialise();
    verify(injector).inject(value);
    if (connectionProvider.isPresent()) {
      verify(injector).inject(connectionProvider.get());
    } else {
      verify(injector, never()).inject(any(ConnectionProvider.class));
    }
  }

  @Test
  public void connectionBound() throws Exception {
    interceptable.initialise();
    assertBound();
  }

  private void assertBound() throws Exception {
    if (connectionProvider.isPresent()) {
      verify(connectionManager, times(1)).bind(value, connectionProvider.get());
    } else {
      verify(connectionManager, never()).bind(same(value), anyObject());
    }
  }

  private VerificationMode getBindingVerificationMode() {
    return connectionProvider.map(p -> times(1)).orElse(never());
  }

  @Test
  public void connectionReBindedAfterStopStart() throws Exception {
    connectionBound();
    interceptable.stop();
    verify(connectionManager, getBindingVerificationMode()).unbind(value);

    reset(connectionManager);
    interceptable.start();
    assertBound();
  }

  @Test
  public void valueInitialised() throws Exception {
    interceptable.initialise();
    verify((Initialisable) value).initialise();
    if (connectionProvider.isPresent()) {
      verify((Initialisable) connectionProvider.get()).initialise();
    }
  }

  @Test
  public void valueStarted() throws Exception {
    interceptable.start();
    verify((Startable) value).start();
    if (connectionProvider.isPresent()) {
      verify((Startable) connectionProvider.get()).start();
    }
  }

  @Test
  public void testConnectivityUponStart() throws Exception {
    if (connectionProvider.isPresent()) {
      valueStarted();
      verify(connectionManager).testConnectivity(interceptable);
    }
  }

  @Test
  public void testConnectivityFailsUponStart() throws Exception {
    if (connectionProvider.isPresent()) {
      Exception connectionException = new ConnectionException("Oops!");
      when(connectionManager.testConnectivity(interceptable))
          .thenReturn(failure(connectionException.getMessage(), UNKNOWN, connectionException));

      try {
        interceptable.start();
        fail("Was expecting connectivity testing to fail");
      } catch (Exception e) {
        verify(connectionManager, times(RECONNECTION_MAX_ATTEMPTS + 1)).testConnectivity(interceptable);
        assertThat(e.getCause(), is(instanceOf(RetryPolicyExhaustedException.class)));
      }
    }
  }

  @Test
  public void valueStopped() throws Exception {
    interceptable.stop();
    verify((Stoppable) value).stop();
    if (connectionProvider.isPresent()) {
      verify((Stoppable) connectionProvider.get()).stop();
    }
  }

  @Test
  public void connectionUnbinded() throws Exception {
    interceptable.stop();
    if (connectionProvider.isPresent()) {
      verify(connectionManager).unbind(value);
    } else {
      verify(connectionManager, never()).unbind(anyObject());
    }
  }

  @Test
  public void valueDisposed() throws Exception {
    interceptable.dispose();
    verify((Disposable) value).dispose();
    if (connectionProvider.isPresent()) {
      verify((Disposable) connectionProvider.get()).dispose();
    }
  }

  @Test
  public void disposeMetadataCacheWhenConfigIsDisposed() throws Exception {
    MuleMetadataManager muleMetadataManager = muleContext.getRegistry().lookupObject(MuleMetadataManager.class);
    muleMetadataManager.getMetadataCache(NAME);
    interceptable.stop();
    new PollingProber(1000, 100).check(new JUnitLambdaProbe(() -> muleMetadataManager.getMetadataCaches().entrySet().isEmpty()));
  }

  @Test
  public void getName() {
    assertThat(interceptable.getName(), is(NAME));
  }

  @Test
  public void getModel() {
    assertThat(interceptable.getModel(), is(sameInstance(configurationModel)));
  }

  @Test
  public void getValue() {
    assertThat(interceptable.getValue(), is(sameInstance(value)));
  }

  @Test(expected = IllegalStateException.class)
  public void getStatsBeforeInit() {
    interceptable.getStatistics();
  }

  @Test
  public void getStatistics() throws Exception {
    interceptable.initialise();
    assertThat(interceptable.getStatistics(), is(notNullValue()));
  }
}
