/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import static com.ibm.wsdl.util.StringUtils.getURL;
import static java.lang.String.format;
import static org.mule.metadata.xml.XmlTypeLoader.XML;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import org.mule.extension.ws.api.WscConnection;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.xml.XmlTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.wsdl.Part;

public class BodyResolver extends BaseWscMetadataResolver implements InputTypeResolver<String> {


  @Override
  public MetadataType getInputMetadata(MetadataContext metadataContext, String operation)
      throws MetadataResolvingException, ConnectionException {
    WscConnection connection = getConnection(metadataContext);
    String wsdlLocation = connection.getWsdlLocation();
    OperationWsdlResolver resolver = new OperationWsdlResolver(wsdlLocation);
    // URL url = getFileUrl(wsdlLocation);
    //Optional<Part> part = resolver.getMessagePart(connection.getPort(), operation, o -> o.getInput().getMessage());
    //return createMetadata(resolver.getSchemas(), part);
    return null;
  }

  private MetadataType createMetadata(List<String> schemas, Optional<Part> partOptional) {
    if (partOptional.isPresent()) {
      XmlTypeLoader xmlTypeLoader = new XmlTypeLoader(new HashSet<>(schemas));
      Part part = partOptional.get();
      if (part.getElementName() != null) {
        return xmlTypeLoader.load(part.getElementName().toString()).orElseThrow(() -> new RuntimeException("Cambiame"));
      } else if (part.getTypeName() != null) {
        return getDataTypeFromTypeName(part);
      }
    }
    return BaseTypeBuilder.create(XML).nullType().build();
  }

  private URL getFileUrl(String fileURI) throws MetadataResolvingException {
    try {
      return getURL(null, fileURI);
    } catch (MalformedURLException e) {
      throw new MetadataResolvingException(format("Could not obtain URL for file [%s]", fileURI), INVALID_CONFIGURATION, e);
    }
  }
}
