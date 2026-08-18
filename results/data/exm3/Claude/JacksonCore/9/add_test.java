// com/fasterxml/jackson/core/json/TestJsonParser.java
public void testGetValueAsTextNullToken() throws Exception {
    JsonFactory f = new JsonFactory();
    JsonParser p = f.createParser("{}");
    assertNull(p.getValueAsString());
    assertEquals("default", p.getValueAsString("default"));
    p.close();
}