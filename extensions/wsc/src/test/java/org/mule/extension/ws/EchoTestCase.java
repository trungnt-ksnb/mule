/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;

import org.junit.Test;

public class EchoTestCase extends WebServiceConsumerTestCase {

  @Override
  protected String getConfigFile() {
    return "config/echo.xml";
  }

  @Test
  public void echoOperation() throws Exception {
    testSoapOperation("echoOperation", "echo.xml");
  }
}
