/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.factories;

import org.mule.runtime.core.api.store.ObjectStore;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueStoreFactoryBean extends ObjectStoreFromRegistryFactoryBean {

  protected transient Logger logger = LoggerFactory.getLogger(getClass());

  public AbstractQueueStoreFactoryBean(String name) {
    super(name);
  }

  @Override
  protected ObjectStore<Serializable> createInstance() throws Exception {
    logger.warn("Queue stores are deprecated and are going to be removed in Mule 4.0");
    return super.createInstance();
  }
}
