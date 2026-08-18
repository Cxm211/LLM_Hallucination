// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testCustomIntegerKeyDeserializer() throws IOException {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Integer.class, new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getIntValue();
            }
        });
        ObjectMapper mapper = new ObjectMapper().registerModule(simpleModule);
        Map<Integer, String> map = mapper.readValue("{\"123\": \"value\"}", new TypeReference<Map<Integer, String>>() {});
        assertEquals("value", map.get(123));
    }
