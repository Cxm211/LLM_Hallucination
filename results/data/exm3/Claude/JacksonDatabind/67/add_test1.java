// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testNonEnumKeyWithModifier() throws IOException
    {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    final JavaType type, KeyDeserializer deserializer)
            {
                // Modifier that processes non-enum types
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt)
                            throws IOException
                    {
                        if (deserializer != null) {
                            return deserializer.deserializeKey(key, ctxt);
                        }
                        return key;
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(module);

        // Test with String key (non-enum)
        Map<String,String> map = mapper.readValue(
                aposToQuotes("{'key1':'value1'}"),
                new TypeReference<Map<String,String>>() { });
        assertEquals(1, map.size());
        assertEquals("value1", map.get("key1"));
    }