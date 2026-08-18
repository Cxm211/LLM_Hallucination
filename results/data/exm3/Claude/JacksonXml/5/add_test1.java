// com/fasterxml/jackson/dataformat/xml/MapperCopyTest.java
static class NoAnnotationPojo {
    public String field = "test";
}

public void testCopyWithNoAnnotation() throws Exception
{
    XmlMapper xmlMapper = newMapper();
    final ObjectMapper xmlMapperCopy = xmlMapper.copy();

    String xml1 = xmlMapper.writeValueAsString(new NoAnnotationPojo());
    String xml2 = xmlMapperCopy.writeValueAsString(new NoAnnotationPojo());

    if (!xml1.contains("NoAnnotationPojo")) {
        fail("Original mapper should use 'NoAnnotationPojo', xml = " + xml1);
    }
    if (!xml2.contains("NoAnnotationPojo")) {
        fail("Copied mapper should use 'NoAnnotationPojo', xml = " + xml2);
    }
}