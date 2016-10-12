/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.transport;

import static org.mule.extension.ws.api.ConsumeOperation.MULE_ATTACHMENTS_KEY;
import static org.mule.extension.ws.api.ConsumeOperation.MULE_HEADERS_KEY;
import org.mule.extension.ws.api.transport.interceptor.MessageDispatcherInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public final class WsConduit extends AbstractConduit {

  private static final Logger LOGGER = Logger.getLogger(WsConduit.class.getSimpleName());

  public WsConduit(EndpointReferenceType t) {
    super(t);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void prepare(Message message) throws IOException {
    message.setContent(OutputStream.class, new ByteArrayOutputStream());
    List<SoapHeader> soapHeaders = (List<SoapHeader>) message.getExchange().get(MULE_HEADERS_KEY);
    soapHeaders.forEach(header -> ((SoapMessage) message).getHeaders().add(header));
    List<Attachment> soapAttachments = (List<Attachment>) message.getExchange().get(MULE_ATTACHMENTS_KEY);
    message.setAttachments(soapAttachments);
    message.getInterceptorChain().add(new MessageDispatcherInterceptor(getMessageObserver()));
  }

  @Override
  public void close(Message msg) throws IOException {
    OutputStream os = msg.getContent(OutputStream.class);
    if (os != null) {
      os.close();
    }
    InputStream is = msg.getContent(InputStream.class);
    if (is != null) {
      is.close();
    }
  }
}
