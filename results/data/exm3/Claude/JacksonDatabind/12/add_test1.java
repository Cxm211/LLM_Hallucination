// com/fasterxml/jackson/databind/deser/TestCustomDeserializers.java
public void testCustomMapDeserWithNullKeyDeser735() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Integer.class, new StdDeserializer<Integer>(Integer.class) {
        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getIntValue() * 50;
        }
    });
    mapper.registerModule(module);
    
    String json = "{\"map1\":{\"a\":1},\"map2\":{\"a\":1}}";
    TestMapBean735 bean = mapper.readValue(json, TestMapBean735.class);
    
    assertEquals(100, bean.map1.get("a").intValue());
    assertEquals(50, bean.map2.get("a").intValue());
}