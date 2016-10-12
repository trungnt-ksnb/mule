/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import org.mule.common.metadata.MetaData;
import org.mule.extension.ws.api.WebServiceConsumer;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;

import com.ibm.wsdl.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.wsdl.Part;

public class WsConsumerMetadataResolver {

  //@Override
  public Set<MetadataKey> getMetadataKeys(MetadataContext metadataContext)
      throws MetadataResolvingException, ConnectionException {
    WebServiceConsumer configuration = getConfiguration(metadataContext);
    return Collections.emptySet();
  }

  //@Override
  public MetadataType getContentMetadata(MetadataContext metadataContext, String operation)
      throws MetadataResolvingException, ConnectionException {
    return null;
  }

  //@Override
  public MetadataType getOutputMetadata(MetadataContext metadataContext, String operation)
      throws MetadataResolvingException, ConnectionException {
    return null;
  }

  private WebServiceConsumer getConfiguration(MetadataContext metadataContext) {
    return metadataContext.<WebServiceConsumer>getConfig().orElseThrow(() -> new RuntimeException("No hay config"));
  }

  //@Override
  public String getCategoryName() {
    return "WebServiceConsumerCategory";
  }

  //private MetaData getMetaData(OperationIOResolver operationIOResolver, MetadataContext metadataContext, String operation) {
  //  try {
  //    WebServiceConsumer configuration = getConfiguration(metadataContext);
  //    OperationWsdlResolver resolver =
  //        new OperationWsdlResolver(operationIOResolver, configuration.getWsdlLocation(), configuration.getService(),
  //                                  configuration.getPort(), operation);
  //    URL url = getFileUrl(configuration.getWsdlLocation());
  //    MetaData metaData = createMetaData(resolver.getSchemas(), resolver.getMessagePart(), url);
  //    //addProperties(metaData, resolver, operationIOResolver.getScope());
  //    return metaData;
  //  } catch (TransformerException e) {
  //    //TODO how to propagate this to the UI? maybe we need a typed exception here
  //    return null;
  //  }
  //}

  private URL getFileUrl(String fileURI) {
    try {
      return StringUtils.getURL(null, fileURI);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private MetaData createMetaData(List<String> schemas, Optional<Part> partOptional, URL url) {
    return null;
  }

  //private void addProperties(MetaData metadata, OperationWsdlResolver invokeWsdlResolver, MetaDataPropertyScope metaDataPropertyScope) {
  //  final List<SOAPHeader> headers = invokeWsdlResolver.getOperationHeaders();
  //  for (SOAPHeader soapHeader : headers) {
  //    final Message message = invokeWsdlResolver.getDefinition().getMessage(soapHeader.getMessage());
  //    if (message != null) {
  //      final Part part = message.getPart(soapHeader.getPart());
  //      metadata.addProperty(metaDataPropertyScope, SoapkitConstants.SOAP_HEADERS_PROPERTY_PREFIX + part.getElementName().getLocalPart(),
  //                           new DefaultXmlMetaDataModel(invokeWsdlResolver.getSchemas(), part.getElementName(), charset()));
  //    }
  //  }
  //}
}
