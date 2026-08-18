// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testCustomEnumKeyDeserializerWithUppercase() throws IOException {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    JavaType type, KeyDeserializer deserializer) {
                if (!type.isEnumType()) {
                    return deserializer;
                }
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, key.toUpperCase());
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        
        // Define a simple enum for testing
        enum TestEnum { VALUE, ANOTHER }
        
        // Test deserialization with lowercase key
        String json = aposToQuotes("{'value':'test'}");
        EnumMap<TestEnum, String> map = mapper.readValue(json, new TypeReference<EnumMap<TestEnum, String>>() { });
        // Expect TestEnum.VALUE to be the key
        assertEquals(TestEnum.VALUE, map.keySet().iterator().next());
    }
