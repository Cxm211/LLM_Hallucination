// com/fasterxml/jackson/databind/deser/TestCustomDeserializers.java
public void testCustomMapKeyDeser735() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addKeyDeserializer(String.class, new KeyDeserializer() {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return key.toUpperCase();
        }
    });
    mapper.registerModule(module);
    
    String json = "{\"map1\":{\"a\":1},\"map2\":{\"a\":1}}";
    TestMapBean735 bean = mapper.readValue(json, TestMapBean735.class);
    
    assertEquals(100, bean.map1.get("a").intValue());
    assertEquals(1, bean.map2.get("A").intValue());
}