/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.metadata;

import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import org.mule.extension.ws.WebServiceConsumerTestCase;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.ProcessorId;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.internal.metadata.MuleMetadataManager;

import org.junit.Before;
import org.junit.Test;

public class WscMetadataTestCase extends WebServiceConsumerTestCase {

  private static final String WSC_CATEGORY = "WebServiceConsumerCategory";

  private static final String[] OPERATIONS = {"echoWithHeaders", "noParams", "fail", "echoAccount", "echo", "noParamsWithHeader"};
  private MuleMetadataManager manager;
  private Event event;

  @Before
  public void init() throws Exception {
    event = eventBuilder().message(InternalMessage.of("")).build();
    manager = muleContext.getRegistry().lookupObject(MuleMetadataManager.class);
  }

  @Override
  protected String getConfigFile() {
    return "config/metadata.xml";
  }

  @Test
  public void getOperationKeys() {
    //MetadataResult<MetadataKeysContainer> result = manager.getMetadataKeys(id("getKeys"));
    //assertThat(result.isSuccess(), is(true));
    //Set<MetadataKey> keys = result.get().getKeys("DefaultMetadataResolverFactory").get();
    //keys.forEach(key -> assertThat(key.getId(), isIn(OPERATIONS)));
  }
  //
  //@Test
  //public void getEchoInputBody() {
  //  MetadataResult<ComponentMetadataDescriptor> result = manager.getMetadata(id("getEchoInputBodyMetadata"), key("echo"));
  //  assertThat(result.isSuccess(), is(true));
  //}

  private MetadataKey key(String id) {
    return newKey(id).build();
  }

  private ProcessorId id(String flow) {
    return new ProcessorId(flow, "0");
  }
}
