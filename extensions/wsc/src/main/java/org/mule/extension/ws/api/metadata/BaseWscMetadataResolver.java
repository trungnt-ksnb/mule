/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import static org.mule.metadata.api.builder.BaseTypeBuilder.create;
import static org.mule.metadata.api.model.MetadataFormat.XML;
import static org.mule.runtime.api.metadata.resolving.FailureCode.CONNECTION_FAILURE;
import org.mule.extension.ws.api.WscConnection;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.NamedTypeResolver;

import java.util.HashMap;
import java.util.Map;

import javax.wsdl.Part;

public class BaseWscMetadataResolver implements NamedTypeResolver {

  private static final String WSC_CATEGORY = "WebServiceConsumerCategory";

  @Override
  public String getCategoryName() {
    return WSC_CATEGORY;
  }

  protected WscConnection getConnection(MetadataContext metadataContext) throws MetadataResolvingException, ConnectionException {
    return metadataContext.<WscConnection>getConnection()
        .orElseThrow(() -> new MetadataResolvingException("Could not obtain connection to retrieve metadata",
                                                          CONNECTION_FAILURE));
  }

  protected MetadataType getDataTypeFromTypeName(Part part) {
    String localPart = part.getTypeName().getLocalPart();
    Map<String, MetadataType> types = new HashMap<>();
    types.put("string", create(XML).stringType().build());
    types.put("boolean", create(XML).booleanType().build());
    types.put("date", create(XML).dateType().build());
    types.put("decimal", create(XML).numberType().build());
    types.put("byte", create(XML).numberType().build());
    types.put("unsignedByte", create(XML).numberType().build());
    types.put("dateTime", create(XML).dateTimeType().build());
    types.put("int", create(XML).numberType().build());
    types.put("integer", create(XML).numberType().build());
    types.put("unsignedInt", create(XML).numberType().build());
    types.put("short", create(XML).numberType().build());
    types.put("unsignedShort", create(XML).numberType().build());
    types.put("long", create(XML).numberType().build());
    types.put("unsignedLong", create(XML).numberType().build());
    types.put("double", create(XML).numberType().build());
    MetadataType type = types.get(localPart);
    return type != null ? type : create(XML).stringType().build();
  }
}
