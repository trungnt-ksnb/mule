/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;

import static javax.xml.ws.Endpoint.publish;
import static org.junit.Assert.assertTrue;
import org.mule.extension.ws.consumer.TestService;

import javax.xml.ws.Endpoint;

import org.custommonkey.xmlunit.XMLUnit;

public class ServiceTestRunner {

  private static final String SERVICE_URL = "http://localhost:6045/testService";

  public static void main(String[] args) {
    XMLUnit.setIgnoreWhitespace(true);
    Endpoint service = publish(SERVICE_URL, new TestService());
    assertTrue(service.isPublished());
  }
}
