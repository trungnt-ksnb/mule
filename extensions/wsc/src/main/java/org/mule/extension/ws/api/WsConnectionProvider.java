/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class WsConnectionProvider implements PoolingConnectionProvider<WscConnection> {

  @Parameter
  private String wsdlLocation;

  @Parameter
  private String service;

  @Parameter
  private String port;

  @Parameter
  @Optional
  private String address;

  @Parameter
  @Optional(defaultValue = "SOAP11")
  private SoapVersion soapVersion;

  @Override
  public WscConnection connect() throws ConnectionException {
    return new WscConnection(wsdlLocation, address, service, port, soapVersion);
  }

  @Override
  public void disconnect(WscConnection client) {}

  @Override
  public ConnectionValidationResult validate(WscConnection client) {
    return success();
  }

}
