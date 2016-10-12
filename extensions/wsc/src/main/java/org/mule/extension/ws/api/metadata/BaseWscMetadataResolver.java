/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import org.mule.runtime.api.metadata.resolving.NamedTypeResolver;

public class BaseWscMetadataResolver implements NamedTypeResolver {

  private static final String WSC_CATEGORY = "WebServiceConsumerCategory";

  @Override
  public String getCategoryName() {
    return WSC_CATEGORY;
  }
}
