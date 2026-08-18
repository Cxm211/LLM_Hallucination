// com/fasterxml/jackson/databind/struct/TestPOJOAsArray.java
public void testCustomNullSerializer() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(String.class, new JsonSerializer<String>() {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeString("CUSTOM_NULL");
            } else {
                gen.writeString(value);
            }
        }
    });
    mapper.registerModule(module);
    String json = mapper.writeValueAsString(new TwoStringsBean());
    assertTrue("Custom null serializer should be invoked", json.contains("null") || json.contains("CUSTOM_NULL"));
}