package org.mule.runtime.config.spring.dsl.model.extension.loader;


import org.mule.runtime.config.spring.dsl.model.ComponentModel;
import org.mule.runtime.config.spring.dsl.model.extension.ModuleExtension;
import org.mule.runtime.config.spring.dsl.processor.xml.XmlCustomAttributeHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;


//TODO WIP-OPERATIONS this class must die and unify with the ModuleLoader one
public class ModulePluginLoader {

  Map<String, ModuleExtension> moduleExtensionMap;

  public ModulePluginLoader(ComponentModel muleRootComponentModel, Properties applicationProperties) {
    this.moduleExtensionMap = modulesPlugins(muleRootComponentModel, applicationProperties);
  }

  public ModuleExtension get(String namespace) {
    return moduleExtensionMap.get(namespace);
  }

  private Map<String, ModuleExtension> modulesPlugins(ComponentModel muleRootComponentModel, Properties applicationProperties) {
    ModuleLoader moduleLoader = new ModuleLoader();
    Map<String, String> schemaLocations = getSchemaLocations(muleRootComponentModel);
    Map<String, ModuleExtension> modulesExtension = new HashMap<>();

    schemaLocations.forEach((namespace, location) -> {
      Optional<ModuleExtension> extensionOptional = moduleLoader.lookup(location, applicationProperties);
      if (extensionOptional.isPresent()) {
        ModuleExtension moduleExtension = extensionOptional.get();
        modulesExtension.put(moduleExtension.getName(), moduleExtension);
      }
    });
    return modulesExtension;
  }

  /**
   * returns a map with the namespaces and locations from the xsi:schemaLocation attribute.
   * If it can't success to do it so, it will throw an exception.
   *
   * @param muleRootComponentModel
   * @return map where the key represents the namespace and the value is the location of the xsd
   */
  private Map<String, String> getSchemaLocations(ComponentModel muleRootComponentModel) {
    String xsiPrefix = null;
    //finds the XSI prefix
    for (Map.Entry<String, String> entry : muleRootComponentModel.getParameters().entrySet()) {
      if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(entry.getValue())) {
        xsiPrefix = StringUtils.removeStart(entry.getKey(), XMLConstants.XMLNS_ATTRIBUTE.concat(":"));
        break;
      }
    }
    if (StringUtils.isBlank(xsiPrefix)) {
      throw new IllegalArgumentException(String
          .format("The file [%s] needs to define the XSI namespace defined (e.g.: xmlns:xsi=\"%s\")",
                  muleRootComponentModel.getCustomAttributes().get(XmlCustomAttributeHandler.CONFIG_FILE_NAME),
                  XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI));
    }

    String schemaLocationKey = xsiPrefix.concat(":schemaLocation");
    String schemaLocationValue = muleRootComponentModel.getParameters().getOrDefault(schemaLocationKey, "");
    String[] splittedSchemaLocation = schemaLocationValue.split("\\s+");

    if (splittedSchemaLocation.length == 0 || ((splittedSchemaLocation.length & 1) != 0)) {
      throw new IllegalArgumentException(String
          .format("The file [%s] schemaLocation it's either missing, empty, or has non even values (it has to be a namespace+location per entry)",
                  muleRootComponentModel.getCustomAttributes().get(XmlCustomAttributeHandler.CONFIG_FILE_NAME)));
    }

    Map<String, String> schemaLocations = new HashMap<>();
    for (int i = 0; i < splittedSchemaLocation.length; i = i + 2) {
      schemaLocations.put(splittedSchemaLocation[i], splittedSchemaLocation[i + 1]);
    }
    return schemaLocations;
  }

}
