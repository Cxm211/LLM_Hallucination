// com/fasterxml/jackson/databind/deser/jdk/MapDeserializerCachingTest.java
public void testCustomValueDeserializer() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String json = aposToQuotes("{'values':['val1','val2']}");
    
    ListHolder model = mapper.readValue(json, ListHolder.class);
    assertEquals(2, model.values.size());
    assertTrue(model.values.contains("val1 (CUSTOM)"));
    assertTrue(model.values.contains("val2 (CUSTOM)"));
}

static class ListHolder {
    @JsonDeserialize(contentUsing = CustomValueDeserializer.class)
    public List<String> values;
}

static class CustomValueDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.getText() + " (CUSTOM)";
    }
}