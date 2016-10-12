/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;


import static java.util.Collections.emptyList;
import static org.mule.runtime.core.util.IOUtils.toDataHandler;
import org.mule.extension.ws.api.exception.WebServiceConsumerException;
import org.mule.extension.ws.api.transport.interceptor.SoapActionInterceptor;
import org.mule.runtime.core.util.collection.ImmutableListCollector;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.runtime.operation.OperationResult;
import org.mule.runtime.module.xml.stax.StaxSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.jaxp.SaxonTransformerFactory;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.message.Attachment;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class ConsumeOperation {

  public static final String MULE_ATTACHMENTS_KEY = "mule.wsc.attachments";
  public static final String MULE_HEADERS_KEY = "mule.wsc.headers";

  public OperationResult<String, WsAttributes> consume(@Connection WsClient wsclient,
                                                       @MetadataKeyId String operationName,
                                                       @Optional String payload,
                                                       @Optional Map<String, String> headers,
                                                       @Optional List<WsAttachment> attachments)
      throws Exception {

    wsclient.addOutInterceptor(new SoapActionInterceptor(operationName));
    Map<String, Object> props = new HashMap<>();
    props.put(MULE_ATTACHMENTS_KEY, transformAttachments(attachments));
    props.put(MULE_HEADERS_KEY, transformHeaders(headers));

    Map<String, Object> ctx = new HashMap<>();
    ctx.put(Client.REQUEST_CONTEXT, props);
    ctx.put(Client.RESPONSE_CONTEXT, props);

    Object[] response = wsclient.invoke(stringToXmlStreamReader(payload), ctx);
    return asResult(processResponse(response), new WsAttributes());
  }

  private OperationResult<String, WsAttributes> asResult(String output, WsAttributes attributes) {
    return OperationResult.<String, WsAttributes>builder().output(output).attributes(attributes).build();
  }

  private String processResponse(Object[] response) throws Exception {
    XMLStreamReader xmlStreamReader = (XMLStreamReader) response[0];
    StaxSource staxSource = new StaxSource(xmlStreamReader);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory idTransformer = new SaxonTransformerFactory();
    Transformer transformer = idTransformer.newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.transform(staxSource, result);
    return writer.getBuffer().toString();
  }

  private List<SoapHeader> transformHeaders(Map<String, String> headers) {
    if (headers == null) {
      return emptyList();
    }
    return headers.entrySet().stream()
        .map(h -> new SoapHeader(new QName(null, h.getKey()), stringToDocument(h.getValue())))
        .collect(new ImmutableListCollector<>());
  }

  private List<Attachment> transformAttachments(List<WsAttachment> attachments) {
    if (attachments == null) {
      return emptyList();
    }
    return attachments.stream().map(a -> {
      try {
        return new AttachmentImpl(a.getId(), toDataHandler(a.getId(), a.getContent(), a.getContentType()));
      } catch (IOException e) {
        throw new WebServiceConsumerException("Error while preparing attachments", e);
      }
    }).collect(new ImmutableListCollector<>());
  }

  private Element stringToDocument(String xml) {
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));
      return db.parse(is).getDocumentElement();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private XMLStreamReader stringToXmlStreamReader(String xml) {
    try {
      if (xml == null) {
        xml = "Aca va un empty content o como garcha sea";
      }
      return XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
