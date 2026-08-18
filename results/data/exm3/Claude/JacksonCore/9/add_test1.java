// com/fasterxml/jackson/core/json/TestJsonParser.java
public void testGetValueAsTextNonString() throws Exception {
    JsonFactory f = new JsonFactory();
    JsonParser p = f.createParser("123");
    p.nextToken();
    assertEquals("123", p.getValueAsString());
    p.close();
    
    p = f.createParser("true");
    p.nextToken();
    assertEquals("true", p.getValueAsString());
    p.close();
    
    p = f.createParser("null");
    p.nextToken();
    assertNull(p.getValueAsString());
    p.close();
}