// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testStringArrayCustomNull() throws Exception
{
    SimpleModule module = new SimpleModule();
    module.addDeserializer(String.class, new JsonDeserializer<String>() {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return jp.getText();
        }
        @Override
        public String getNullValue(DeserializationContext ctxt) {
            return "NULL";
        }
    });
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
    
    String[] result = mapper.readValue("[\"a\", null, \"b\"]", String[].class);
    assertNotNull(result);
    assertEquals(3, result.length);
    assertEquals("a", result[0]);
    assertEquals("NULL", result[1]);
    assertEquals("b", result[2]);
}
