/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.transform.TransformerException;

public class OperationWsdlResolver {

  private OperationIOResolver operationIOResolver;
  private Definition definition;
  private List<String> schemas;
  private Message message;
  private List<SOAPHeader> operationHeaders;
  private Part messagePart;

  public OperationWsdlResolver(OperationIOResolver operationIOResolver, final String wsdlLocation, final String serviceName,
                               final String portName, final String operationName)
      throws TransformerException {
    initialize(operationIOResolver, wsdlLocation, serviceName, portName, operationName);
  }

  private void initialize(OperationIOResolver operationIOResolver, String wsdlLocation, String serviceName, String portName,
                          String operationName)
      throws TransformerException {
    //this.operationIOResolver = operationIOResolver;
    //
    //this.definition = WsdlUtils.parseWSDL(wsdlLocation);
    //this.schemas = WsdlSchemaUtils.getSchemas(definition);
    //Service service = WsdlUtils.getService(definition, serviceName);
    //Port port = WsdlUtils.getPort(service, portName);
    //
    //Binding binding = port.getBinding();
    //PortType portType = binding.getPortType();
    //Operation operation = WsdlUtils.getOperation(portType, operationName);
    //
    //this.message = operationIOResolver.getMessage(operation);
    //WsdlUtils.validateNotNull(message, "There was an error while trying to resolve the message for the ["+operationName+"] operation.");
    //
    //BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
    //operationHeaders = operationIOResolver.getHeaders(bindingOperation);
    //
    //messagePart = resolveMessagePart(bindingOperation);
  }

  private Optional<Part> resolveMessagePart(BindingOperation bindingOperation) {
    Map<?, ?> parts = message.getParts();
    if (!parts.isEmpty()) {
      if (parts.size() == 1) {
        //hack to behave the same way as before when the message has just one part
        Object firstValueKey = parts.keySet().toArray()[0];
        return Optional.of((Part) parts.get(firstValueKey));
      } else {
        Optional<String> bodyPartNameOptional = operationIOResolver.getBodyPartName(bindingOperation);
        if (bodyPartNameOptional.isPresent()) {
          return Optional.of((Part) parts.get(bodyPartNameOptional.get()));
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  //getters
  public Definition getDefinition() {
    return definition;
  }

  public List<String> getSchemas() {
    return schemas;
  }

  public Optional<Part> getMessagePart() {
    return Optional.ofNullable(messagePart);
  }

  public List<SOAPHeader> getOperationHeaders() {
    return operationHeaders;
  }
}
