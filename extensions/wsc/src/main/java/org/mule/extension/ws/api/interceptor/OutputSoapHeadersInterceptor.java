/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.interceptor;

import org.mule.extension.ws.api.ConsumeOperation;
import org.mule.runtime.core.util.collection.ImmutableListCollector;

import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.jaxp.SaxonTransformerFactory;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Node;

/**
 * CXF interceptor that adds inbound properties to the Mule message based on the SOAP headers received in the response.
 */
public class OutputSoapHeadersInterceptor extends AbstractSoapInterceptor {

  public OutputSoapHeadersInterceptor() {
    super(Phase.PRE_PROTOCOL);
  }

  @Override
  public void handleMessage(SoapMessage message) throws Fault {
    List<String> result = message.getHeaders().stream()
      .filter(header -> header instanceof SoapHeader)
      .map(this::nodeToString)
      .collect(new ImmutableListCollector<>());

    message.getExchange().put(ConsumeOperation.MULE_HEADERS_KEY, result);
  }

  private String nodeToString(Header header) {
    try {
      StringWriter writer = new StringWriter();
      Node node = (Node) header.getObject();
      DOMSource source = new DOMSource(node);
      StreamResult result = new StreamResult(writer);
      TransformerFactory idTransformer = new SaxonTransformerFactory();
      Transformer transformer = idTransformer.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.transform(source, result);
      return writer.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
