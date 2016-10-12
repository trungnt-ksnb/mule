/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import org.mule.common.metadata.MetaDataPropertyScope;

import java.util.List;
import java.util.Optional;

import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;

public class InputOperationResolver extends AbstractOperationIOResolver {

  @Override
  public Message getMessage(Operation operation) {
    return operation.getInput().getMessage();
  }

  @Override
  protected Optional<List> extensibilityElements(BindingOperation bindingOperation) {
    if (bindingOperation == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(bindingOperation.getBindingInput().getExtensibilityElements());
  }

  @Override
  public MetaDataPropertyScope getScope() {
    return MetaDataPropertyScope.INBOUND;
  }
}

