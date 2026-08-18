// com/fasterxml/jackson/databind/deser/jdk/MapDeserializerCachingTest.java
public void testMapWithBothKeyAndValueHandlers() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String json = aposToQuotes("{'combined':{'key1':'value1','key2':'value2'}}");
    
    CombinedMapHolder model = mapper.readValue(json, CombinedMapHolder.class);
    assertTrue(model.combined.containsKey("key1 (CUSTOM)"));
    assertTrue(model.combined.containsKey("key2 (CUSTOM)"));
    assertEquals("value1 (CUSTOM)", model.combined.get("key1 (CUSTOM)"));
    assertEquals("value2 (CUSTOM)", model.combined.get("key2 (CUSTOM)"));
}

static class CombinedMapHolder {
    @JsonDeserialize(keyUsing = CustomKeyDeserializer.class, contentUsing = CustomValueDeserializer.class)
    public Map<String, String> combined;
}