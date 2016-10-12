/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;

/**
 * @author MuleSoft, Inc.
 */
public abstract class AbstractOperationIOResolver implements OperationIOResolver {

  @Override
  public List<SOAPHeader> getHeaders(BindingOperation bindingOperation) {
    List<SOAPHeader> result = new ArrayList<>();

    Optional<List> extensibilityElementsOptional = extensibilityElements(bindingOperation);
    if (extensibilityElementsOptional.isPresent()) {
      final List<?> extensibilityElements = extensibilityElementsOptional.get();
      for (Object element : extensibilityElements) {
        if (element != null && element instanceof SOAPHeader) {
          result.add((SOAPHeader) element);
        }
      }
    }

    return result;
  }

  @Override
  public Optional<String> getBodyPartName(BindingOperation bindingOperation) {
    Optional<List> listOptional = extensibilityElements(bindingOperation);
    if (!listOptional.isPresent()) {
      return Optional.empty();
    }
    for (Object object : listOptional.get()) {
      if (object instanceof SOAPBody) { //TODO what about other type of SOAP body out there? (e.g.: SOAP12Body)
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

  protected abstract Optional<List> extensibilityElements(BindingOperation bindingOperation);

}
