// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java::testCustomEnumKeyOnlyViaModifierLowercase
public void testCustomEnumKeyOnlyViaModifierLowercase() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    final JavaType type, KeyDeserializer deserializer) {
                if (!type.isEnumType()) {
                    return deserializer;
                }
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, key.toLowerCase());
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        EnumMap<KeyEnum,String> map = mapper.readValue(
                aposToQuotes("{'replacements':'x'}"),
                new TypeReference<EnumMap<KeyEnum,String>>() { });
        assertEquals(1, map.size());
        assertSame(KeyEnum.replacements, map.keySet().iterator().next());
    }