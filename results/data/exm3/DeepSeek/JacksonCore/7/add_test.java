// com/fasterxml/jackson/core/json/GeneratorFailTest.java
public void testWriteObjectWithTwoFields() throws Exception {
    StringWriter sw = new StringWriter();
    JsonGenerator gen = F.createGenerator(sw);
    gen.writeStartObject();
    gen.writeFieldName("f1");
    gen.writeString("v1");
    gen.writeFieldName("f2");
    gen.writeString("v2");
    gen.writeEndObject();
    gen.close();
    assertEquals("{\"f1\":\"v1\",\"f2\":\"v2\"}", sw.toString());
}
