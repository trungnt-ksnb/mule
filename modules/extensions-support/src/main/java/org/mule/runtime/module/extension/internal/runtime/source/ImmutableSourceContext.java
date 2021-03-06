/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.source;

import org.mule.runtime.api.execution.ExceptionCallback;
import org.mule.runtime.extension.api.runtime.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.MessageHandler;
import org.mule.runtime.extension.api.runtime.source.SourceContext;

import java.util.Optional;

final class ImmutableSourceContext implements SourceContext {

  private final MessageHandler messageHandler;
  private final ExceptionCallback<Void, Throwable> exceptionCallback;
  private final Optional<ConfigurationInstance> configurationInstance;

  ImmutableSourceContext(MessageHandler messageHandler,
                         ExceptionCallback<Void, Throwable> exceptionCallback,
                         Optional<ConfigurationInstance> configurationInstance) {

    this.messageHandler = messageHandler;
    this.exceptionCallback = exceptionCallback;
    this.configurationInstance = configurationInstance;
  }

  @Override
  public MessageHandler getMessageHandler() {
    return messageHandler;
  }

  @Override
  public ExceptionCallback<Void, Throwable> getExceptionCallback() {
    return exceptionCallback;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ConfigurationInstance> getConfigurationInstance() {
    return configurationInstance;
  }
}
