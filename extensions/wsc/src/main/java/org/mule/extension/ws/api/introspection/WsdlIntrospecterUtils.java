/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.introspection;


import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;

public class WsdlIntrospecterUtils {

  //public Service resolveService(ServiceDefinition serviceDefinition, Definition definition) {
  //  return serviceDefinition.getService().isPresent() ? WsdlIntrospecter.getService(definition, serviceDefinition.getService().get())
  //      : lookUpServiceOn(serviceDefinition, definition);
  //}
  //
  //public Port resolvePort(ServiceDefinition serviceDefinition, Service service) {
  //  return serviceDefinition.getPort().isPresent() ? WsdlIntrospecter.getPort(service, serviceDefinition.getPort().get())
  //      : lookUpPortOn(serviceDefinition, service);
  //}
  //
  //public String resolveServiceName(ServiceDefinition serviceDefinition, Definition definition) {
  //  return lookUpServiceNameOn(serviceDefinition, definition);
  //}
  //
  //public String resolvePortName(ServiceDefinition serviceDefinition, Service service) {
  //  return lookUpPortNameOn(serviceDefinition, service);
  //}
  //
  //private String lookUpServiceNameOn(ServiceDefinition serviceDefinition, Definition definition) {
  //  String[] serviceNames = WsdlIntrospecter.getServiceNames(definition);
  //
  //  if (serviceNames.length > 1) {
  //    String errorMessage = String
  //        .format("WSDL file [%s] has [%d] services in it. When providing more than 1 service, the connector should specify which one is the intended to be used.",
  //                serviceDefinition.getWsdlUrl().toString(), serviceNames.length);
  //    throw new IllegalArgumentException(errorMessage);
  //  } else if (serviceNames.length == 0) {
  //    String errorMessage =
  //        String.format("WSDL file [%s] does not have any services at all. Check if the WSDL file was properly generated.",
  //                      serviceDefinition.getWsdlUrl().toString());
  //    throw new IllegalArgumentException(errorMessage);
  //  }
  //
  //  return serviceNames[0];
  //}
  //
  //private Service lookUpServiceOn(ServiceDefinition serviceDefinition, Definition definition) {
  //  return WsdlIntrospecter.getService(definition, lookUpServiceNameOn(serviceDefinition, definition));
  //}
  //
  //private String lookUpPortNameOn(ServiceDefinition serviceDefinition, Service service) {
  //  String[] portNames = WsdlIntrospecter.getPortNames(service);
  //
  //  if (portNames.length > 1) {
  //    String errorMessage = String
  //        .format("WSDL file [%s] has [%d] ports in it. When providing more than 1 port, the connector should specify which one is the intended to be used.",
  //                serviceDefinition.getWsdlUrl().toString(), portNames.length);
  //    throw new IllegalArgumentException(errorMessage);
  //  } else if (portNames.length == 0) {
  //    String errorMessage =
  //        String.format("WSDL file [%s] does not have any port at all. Check if the WSDL file was properly generated.",
  //                      serviceDefinition.getWsdlUrl().toString());
  //    throw new IllegalArgumentException(errorMessage);
  //  }
  //
  //  return portNames[0];
  //}
  //
  //private Port lookUpPortOn(ServiceDefinition serviceDefinition, Service service) {
  //  return WsdlIntrospecter.getPort(service, lookUpPortNameOn(serviceDefinition, service));
  //}
}
