// com/fasterxml/jackson/core/json/GeneratorFailTest.java
public void testWriteRootValueNoLeadingSpace() throws Exception {
    StringWriter sw = new StringWriter();
    JsonGenerator gen = F.createGenerator(sw);
    gen.writeString("hello");
    gen.close();
    assertEquals("\"hello\"", sw.toString());
}
