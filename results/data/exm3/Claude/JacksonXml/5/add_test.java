// com/fasterxml/jackson/dataformat/xml/MapperCopyTest.java
@JsonRootName("CustomRoot")
static class CustomRootPojo {
    public int value = 42;
}

public void testCopyWithCustomRootName() throws Exception
{
    XmlMapper xmlMapper = newMapper();
    final ObjectMapper xmlMapperCopy = xmlMapper.copy();

    String xml1 = xmlMapper.writeValueAsString(new CustomRootPojo());
    String xml2 = xmlMapperCopy.writeValueAsString(new CustomRootPojo());

    if (!xml1.contains("CustomRoot")) {
        fail("Original mapper should use 'CustomRoot', xml = " + xml1);
    }
    if (!xml2.contains("CustomRoot")) {
        fail("Copied mapper should use 'CustomRoot', xml = " + xml2);
    }
}