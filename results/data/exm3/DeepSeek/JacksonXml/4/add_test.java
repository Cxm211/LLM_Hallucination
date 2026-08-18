// com/fasterxml/jackson/dataformat/xml/misc/RootNameTest.java
public void testNullWithDifferentRootName() throws IOException {
    ObjectWriter w = _xmlMapper.writer().withRootName("test");
    String xml = w.writeValueAsString(null);
    assertEquals("<test/>", xml);
}
