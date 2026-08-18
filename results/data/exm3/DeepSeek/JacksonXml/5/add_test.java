// com/fasterxml/jackson/dataformat/xml/MapperCopyTest.java
public void testCopyWithIndependence() throws Exception {
    XmlMapper xmlMapper = newMapper();
    XmlMapper xmlMapperCopy = xmlMapper.copy();
    xmlMapperCopy.disable(MapperFeature.USE_ANNOTATIONS);
    
    String xmlOriginal = xmlMapper.writeValueAsString(new Pojo282());
    String xmlCopy = xmlMapperCopy.writeValueAsString(new Pojo282());
    
    if (!xmlOriginal.contains("AnnotatedName")) {
        fail("Original mapper should use name 'AnnotatedName', xml = " + xmlOriginal);
    }
    if (!xmlCopy.contains("Pojo282") || xmlCopy.contains("AnnotatedName")) {
        fail("Copy mapper should NOT use name 'AnnotatedName' but 'Pojo282', xml = " + xmlCopy);
    }
}
