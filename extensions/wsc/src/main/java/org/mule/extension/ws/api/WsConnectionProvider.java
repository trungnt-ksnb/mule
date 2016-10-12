/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import static org.mule.runtime.api.connection.ConnectionExceptionCode.DISCONNECTED;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class WsConnectionProvider implements PoolingConnectionProvider<WsClient> {

  @Parameter
  @Optional
  private String address;

  @Parameter
  @Optional
  private String service;

  @Parameter
  @Optional
  private String port;

  @Parameter
  @Optional(defaultValue = "1.1")
  private String soapVersion;

  @Parameter
  @Optional
  private String wsdlLocation;

  @Override
  public WsClient connect() throws ConnectionException {
    return new WsClient(wsdlLocation, address, service, port, soapVersion);
  }

  @Override
  public void disconnect(WsClient client) {}

  @Override
  public ConnectionValidationResult validate(WsClient client) {
    return client != null ? success() : failure("null client", DISCONNECTED, null);
  }

}
