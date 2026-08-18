// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testCustomBooleanKeyDeserializer() throws IOException {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Boolean.class, new JsonDeserializer<Boolean>() {
            @Override
            public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getBooleanValue();
            }
        });
        ObjectMapper mapper = new ObjectMapper().registerModule(simpleModule);
        Map<Boolean, String> map = mapper.readValue("{\"true\": \"yes\"}", new TypeReference<Map<Boolean, String>>() {});
        assertEquals("yes", map.get(true));
    }
