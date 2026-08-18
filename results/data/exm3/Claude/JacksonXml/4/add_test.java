// com/fasterxml/jackson/dataformat/xml/misc/RootNameTest.java
public void testDynamicRootNameWithNullOnly() throws IOException
{
    ObjectWriter w = _xmlMapper.writer().withRootName("custom");
    String xml = w.writeValueAsString(null);
    assertEquals("<custom/>", xml);
}