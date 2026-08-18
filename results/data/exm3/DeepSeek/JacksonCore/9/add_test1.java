// com/fasterxml/jackson/core/json/TestJsonParser.java
public void testGetValueAsStringWithDefaultValue() throws Exception {
    JsonFactory f = new JsonFactory();
    JsonParser parser = f.createParser("{}");
    parser.nextToken();
    String value = parser.getValueAsString("default");
    assertEquals("default", value);
    parser.close();
}
