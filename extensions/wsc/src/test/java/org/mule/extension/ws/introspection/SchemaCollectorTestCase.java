/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.introspection;

import org.mule.extension.ws.api.introspection.WsdlIntrospecter;

import javax.xml.ws.Endpoint;

public class SchemaCollectorTestCase {

  private static Endpoint service;
  private static WsdlIntrospecter wsdlIntrospecter = new WsdlIntrospecter();

  //@BeforeClass
  //public static void startService() throws MuleException {
  //  XMLUnit.setIgnoreWhitespace(true);
  //  if (service == null) {
  //    service = withContextClassLoader(ClassLoader.getSystemClassLoader(), () -> publish(SERVICE_URL, new TestService()));
  //  }
  //  assertTrue(service.isPublished());
  //}
  //
  //public static void main(String[] args) {
  //  service = withContextClassLoader(ClassLoader.getSystemClassLoader(), () -> publish(SERVICE_URL, new TestService()));
  //}
  //
  //@Test
  //public void getSchemas() {
  //  Definition definition = wsdlIntrospecter.getWsdlDefinition(WSDL_URL);
  //  WsdlSchemaCollector schemaCollector = new WsdlSchemaCollector();
  //  List<String> schemas = schemaCollector.getSchemas(definition);
  //  assertThat(schemas, hasSize(1));
  //}
  //
  //@AfterClass
  //public static void stopService() {
  //  service.stop();
  //}


}
