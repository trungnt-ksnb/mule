/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;

import static javax.xml.ws.Endpoint.publish;
import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mule.runtime.core.util.ClassUtils.withContextClassLoader;
import org.mule.extension.ws.consumer.TestService;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Endpoint;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class WebServiceConsumerTestCase extends MuleArtifactFunctionalTestCase {

  public static final String SERVICE_URL = "http://localhost:6045/testService";
  public static final String WSDL_URL = SERVICE_URL + "?wsdl";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static Endpoint service;

  @BeforeClass
  public static void startService() throws MuleException {
    XMLUnit.setIgnoreWhitespace(true);
    if (service == null) {
      service = withContextClassLoader(ClassLoader.getSystemClassLoader(), () -> publish(SERVICE_URL, new TestService()));
    }
    assertTrue(service.isPublished());
  }

  public static void stopService() {
    service.stop();
  }

  protected void testSoapOperation(String name, String xmlName) throws Exception {
    Message message = flowRunner(name).withPayload(resourceAsString("request/" + xmlName)).run().getMessage();
    String out = (String) message.getPayload().getValue();
    assertSimilarXml("response/" + xmlName, out);
  }

  protected void assertSimilarXml(String expectedResource, String output) throws Exception {
    String expected = resourceAsString(expectedResource);
    Diff diff = compareXML(output, expected);
    if (!diff.similar()) {
      System.out.println("Expected xml is: \n");
      System.out.println(prettyPrint(expected));
      System.out.println("\n\n\n Output is: \n");
      System.out.println(prettyPrint(output));
    }
    assertThat(diff.similar(), is(true));
  }

  private String prettyPrint(String a) throws TransformerException, ParserConfigurationException, IOException, SAXException {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(a));
    Document doc = db.parse(is);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    //initialize StreamResult with File object to save to file
    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(doc);
    transformer.transform(source, result);
    return result.getWriter().toString();
  }

  protected String resourceAsString(final String resource) throws XMLStreamException, IOException {
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer);
    return writer.toString();
  }
}
