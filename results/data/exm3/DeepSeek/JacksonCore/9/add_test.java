// com/fasterxml/jackson/core/json/TestJsonParser.java
public void testGetValueAsStringLargeString() throws Exception {
    JsonFactory f = new JsonFactory();
    f.setBufferSize(100);
    String largeString = new String(new char[1000]).replace('\0', 'a');
    String json = "\"" + largeString + "\"";
    JsonParser parser = f.createParser(json);
    parser.nextToken();
    String value = parser.getValueAsString();
    assertEquals(largeString, value);
    parser.close();
}
