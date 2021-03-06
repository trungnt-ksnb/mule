/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file;

import org.mule.extension.file.api.LocalFileAttributes;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.runtime.api.metadata.MetadataManager;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.test.extension.file.common.FileMetadataResolverTestCommon;

import org.junit.Before;
import org.junit.Test;

public class FileMetadataResolverTestCase extends FileConnectorTestCase {

  private MetadataManager manager;
  private FileMetadataResolverTestCommon testCommon;

  @Override
  protected String getConfigFile() {
    return "file-metadata-config.xml";
  }

  @Before
  public void setupManager() throws RegistrationException {
    manager = muleContext.getRegistry().lookupObject(MetadataManager.class);
    testCommon = new FileMetadataResolverTestCommon();
  }

  @Test
  public void getReadAttributesMetadata() {
    testCommon.testReadAttributesMetadata(manager, FileAttributes.class);
  }

  @Test
  public void getListOperationOutputMetadata() {
    testCommon.testTreeNodeType(manager, LocalFileAttributes.class);
  }
}
