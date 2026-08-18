// com/fasterxml/jackson/dataformat/xml/misc/RootNameTest.java
public void testDynamicRootNameWithDifferentNames() throws IOException
{
    ObjectWriter w1 = _xmlMapper.writer().withRootName("alpha");
    String xml1 = w1.writeValueAsString(null);
    assertEquals("<alpha/>", xml1);
    
    ObjectWriter w2 = _xmlMapper.writer().withRootName("beta");
    String xml2 = w2.writeValueAsString(null);
    assertEquals("<beta/>", xml2);
}