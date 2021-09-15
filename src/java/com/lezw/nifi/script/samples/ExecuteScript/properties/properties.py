# ExecuteScript 读取 nifi.properties 中的配置项

from org.apache.nifi.properties import NiFiPropertiesLoader

flowFile = session.get()
if flowFile != None:

    # Get property to read
    propertyName = flowFile.getAttribute("property-name")

    # Get NiFi properties
    nifiPropertiesLoader = NiFiPropertiesLoader()
    nifiProperties = nifiPropertiesLoader.get()

    # Read property value
    propertyValue = nifiProperties.getProperty(propertyName)
    flowFile = session.putAttribute(flowFile, "property-value", propertyValue)

    session.transfer(flowFile, REL_SUCCESS)