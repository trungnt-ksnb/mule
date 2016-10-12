/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import org.mule.extension.ws.api.transport.WscTransportFactory;
import org.mule.extension.ws.api.transport.interceptor.NamespaceRestorerStaxInterceptor;
import org.mule.extension.ws.api.transport.interceptor.NamespaceSaverStaxInterceptor;
import org.mule.extension.ws.api.transport.interceptor.StreamClosingInterceptor;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.interceptor.CheckFaultInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.service.model.BindingOperationInfo;

public class WsClient {

  private final Client client;
  private final String wsdlLocation;
  private final String service;
  private final String port;
  private final String soapVersion;

  public WsClient(String wsdlLocation, String address, String service, String port, String soapVersion) {
    this.wsdlLocation = wsdlLocation;
    this.service = service;
    this.port = port;
    this.soapVersion = soapVersion;
    this.client = new WscTransportFactory().createClient(wsdlLocation, address, soapVersion);
    setupClient();
  }

  public Object[] invoke(Object payload, Map<String, Object> ctx) throws Exception {
    return client.invoke(getOperation("invoke"), new Object[] {payload}, ctx);
  }

  public void addOutInterceptor(Interceptor<? extends Message> i) {
    client.getOutInterceptors().add(i);
  }

  public void addInInterceptor(Interceptor<? extends Message> i) {
    client.getInInterceptors().add(i);
  }

  private BindingOperationInfo getOperation(String operationName) throws Exception {
    // Normally its not this hard to invoke the CXF Client, but we're
    // sending along some exchange properties, so we need to use a more advanced
    // method
    Endpoint ep = client.getEndpoint();
    QName q = new QName(ep.getService().getName().getNamespaceURI(), operationName);
    BindingOperationInfo bop = ep.getBinding().getBindingInfo().getOperation(q);
    if (bop.isUnwrappedCapable()) {
      bop = bop.getUnwrappedOperation();
    }
    return bop;
  }

  private void setupClient() {
    addInInterceptor(new NamespaceRestorerStaxInterceptor());
    addInInterceptor(new NamespaceSaverStaxInterceptor());
    addInInterceptor(new StreamClosingInterceptor());
    addInInterceptor(new CheckFaultInterceptor());

    //client.getInInterceptors().add(new CopyAttachmentInInterceptor());
    //client.getOutInterceptors().add(new OutputPayloadInterceptor());
    //client.getOutInterceptors().add(new CopyAttachmentOutInterceptor());

    Binding binding = client.getEndpoint().getBinding();
    removeInterceptor(binding.getOutInterceptors(), WrappedOutInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), Soap11FaultInInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), Soap12FaultInInterceptor.class.getName());
    removeInterceptor(binding.getInInterceptors(), CheckFaultInterceptor.class.getName());
  }

  private void removeInterceptor(List<Interceptor<? extends Message>> inInterceptors, String name) {
    inInterceptors.stream()
        .filter(i -> i instanceof PhaseInterceptor)
        .filter(i -> ((PhaseInterceptor) i).getId().equals(name))
        .findFirst()
        .ifPresent(inInterceptors::remove);
  }

  public String getWsdlLocation() {
    return wsdlLocation;
  }

  public String getService() {
    return service;
  }

  public String getPort() {
    return port;
  }
}
