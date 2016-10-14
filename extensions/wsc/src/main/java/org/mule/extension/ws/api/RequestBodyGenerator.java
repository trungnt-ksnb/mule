/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import static java.util.Collections.emptyList;
import org.mule.common.metadata.DefaultXmlMetaDataModel;
import org.mule.common.metadata.MetaDataGenerationException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the request body for an operation of a web service when no parameters are required.
 */
public class RequestBodyGenerator {

  private static final Logger logger = LoggerFactory.getLogger(RequestBodyGenerator.class);

  private final Port port;
  private final String operation;
  private final List<String> schemas;

  public RequestBodyGenerator(Port port, String operation, List<String> schemas) {
    this.port = port;
    this.operation = operation;
    this.schemas = schemas;
  }

  /**
   * Checks if the operation requires input parameters (if the XML required in the body is just one constant element). If so, the
   * body with this XML will be returned in order to send it in every request instead of the payload.
   */
  public String generateRequest() {

    BindingOperation bindingOperation = port.getBinding().getBindingOperation(operation, null, null);

    List<String> soapBodyParts = getSoapBodyParts(bindingOperation);

    if (soapBodyParts == null) {
      logger.warn("No SOAP body defined in the WSDL for the specified operation, cannot check if the operation "
          + "requires input parameters. The payload will be used as SOAP body.");
      return null;
    }

    Part part = getSinglePart(soapBodyParts, bindingOperation.getOperation().getInput().getMessage());

    if (part == null || part.getElementName() == null) {
      // There is no single part in the message, or there is one part with no element defined (therefore has
      // a single type and the web service requires a parameter).
      return null;
    }


    try {
      DefaultXmlMetaDataModel model = new DefaultXmlMetaDataModel(schemas, part.getElementName(), Charset.defaultCharset());

      if (model.getFields().isEmpty()) {
        logger.info("The selected operation does not require input parameters, the payload will be ignored");
        QName element = part.getElementName();
        return String.format("<ns:%s xmlns:ns=\"%s\" />", element.getLocalPart(), element.getNamespaceURI());
      }
    } catch (MetaDataGenerationException e) {
      logger.warn("Unable to check if the operation requires input parameters, the payload will be used as SOAP body", e);
    }
    return null;
  }

  /**
   * Finds the part of the input message that must be used in the SOAP body, if the operation requires only one part. Otherwise
   * returns null.
   */
  private Part getSinglePart(List<String> soapBodyParts, javax.wsdl.Message inputMessage) {
    if (soapBodyParts.isEmpty()) {
      Map parts = inputMessage.getParts();

      if (parts.size() == 1) {
        return (Part) parts.values().iterator().next();
      }
    } else {
      if (soapBodyParts.size() == 1) {
        String partName = soapBodyParts.get(0);
        return inputMessage.getPart(partName);
      }
    }

    return null;
  }

  /**
   * Retrieves the list of SOAP body parts of a binding operation, or null if there is no SOAP body defined.
   */
  public static List<String> getSoapBodyParts(BindingOperation bindingOperation) {
    List elements = bindingOperation.getBindingInput().getExtensibilityElements();
    for (Object element : elements) {
      if (element instanceof SOAPBody) {
        return ((SOAPBody) element).getParts();
      }
      if (element instanceof SOAP12Body) {
        return ((SOAP12Body) element).getParts();
      }
    }
    return emptyList();
  }
}
