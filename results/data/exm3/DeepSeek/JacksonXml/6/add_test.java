// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java
public void testAttributeWith0Bytes() throws Exception {
    @com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement(localName = "TestPojo")
    static class AttrPojo {
        @com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty(isAttribute = true)
        public byte[] field;
    }
    AttrPojo pojo = new AttrPojo();
    pojo.field = new byte[0];
    String xml = MAPPER.writeValueAsString(pojo);
    assertEquals("<TestPojo field=\"\"/>", xml);
}
