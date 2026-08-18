        public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) { // is this even legal call?
                return null;
            }
            try {
                // Ugh... should not have to give parser which may or may not be correct one...
                Object result = _delegate.deserialize(ctxt.getParser(), ctxt);
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
            } catch (Exception re) {
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
            }
        }

// trigger testcase
public void testCustomEnumKeySerializerWithPolymorphic() throws IOException
    {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                    throws IOException
            {
                return SuperTypeEnum.valueOf(p.getText());
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(simpleModule);

        SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"bar\"}}",
                SuperType.class);
        assertEquals("Deserialized someMap.FOO should equal bar", "bar",
                superType.someMap.get(SuperTypeEnum.FOO));
    }
