/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import org.mule.extension.ws.api.introspection.WsdlIntrospecter;
import org.mule.extension.ws.api.introspection.WsdlSchemaCollector;
import org.mule.runtime.core.util.collection.ImmutableListCollector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;

public class OperationWsdlResolver {

  private static final WsdlSchemaCollector schemaCollector = new WsdlSchemaCollector();
  private static final WsdlIntrospecter introspecter = new WsdlIntrospecter();

  private final List<String> schemas;

  public OperationWsdlResolver(String wsdlLocation) {
    this.schemas = schemaCollector.getSchemas(introspecter.getWsdlDefinition(wsdlLocation));
  }

  @SuppressWarnings("unchecked")
  public List<SOAPHeader> getHeaders(BindingOperation bindingOperation) {
    Optional<List> extensibilityElements = extensibilityElements(bindingOperation);
    return (List<SOAPHeader>) extensibilityElements
        .map(elements -> elements.stream()
            .filter(e -> e != null && e instanceof SOAPHeader)
            .collect(new ImmutableListCollector()))
        .orElse(emptyList());
  }

  public Optional<Part> getMessagePart(Port port, String operationName, Function<Operation, Message> messageRetriever) {
    Binding binding = port.getBinding();
    PortType portType = binding.getPortType();
    Operation operation = introspecter.getOperation(portType, operationName);
    Message message = messageRetriever.apply(operation);

    if (message == null) {
      //WsdlUtils.validateNotNull(message, "There was an error while trying to resolve the message for the ["+operationName+"] operation.");
    }

    BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
    //operationHeaders = operationIOResolver.getHeaders(bindingOperation);
    return resolveMessagePart(bindingOperation, message);
  }

  private Optional<Part> resolveMessagePart(BindingOperation bindingOperation, Message message) {
    Map parts = message.getParts();
    if (!parts.isEmpty()) {
      if (parts.size() == 1) {
        //hack to behave the same way as before when the message has just one part
        Object firstValueKey = parts.keySet().toArray()[0];
        return of((Part) parts.get(firstValueKey));
      } else {
        Optional<String> bodyPartNameOptional = getBodyPartName(bindingOperation);
        if (bodyPartNameOptional.isPresent()) {
          return of((Part) parts.get(bodyPartNameOptional.get()));
        }
      }
    }
    return empty();
  }

  private Optional<String> getBodyPartName(BindingOperation bindingOperation) {
    Optional<List> listOptional = extensibilityElements(bindingOperation);
    if (!listOptional.isPresent()) {
      return Optional.empty();
    }
    for (Object object : listOptional.get()) {
      //TODO what about other type of SOAP body out there? (e.g.: SOAP12Body)
      if (object instanceof SOAPBody) {
        SOAPBody soapBody = (SOAPBody) object;
        List soapBodyParts = soapBody.getParts();
        if (soapBodyParts.size() > 1) {
          throw new RuntimeException("Warning: Operation Messages With More Than 1 Part Are Not Supported.");
        }
        if (soapBodyParts.isEmpty()) {
          return Optional.empty();
        }
        String partName = (String) soapBodyParts.get(0);
        return Optional.of(partName);
      }
    }
    return Optional.empty();
  }

  private Optional<List> extensibilityElements(BindingOperation bindingOperation) {
    if (bindingOperation == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(bindingOperation.getBindingOutput().getExtensibilityElements());
  }

  public List<String> getSchemas() {
    return schemas;
  }
}
