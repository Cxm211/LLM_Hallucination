// com/fasterxml/jackson/databind/deser/TestCollectionDeserialization.java::testArrayIndexForExceptions
public void testArrayIndexForExceptionsWithCustomDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule m = new SimpleModule();
        m.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                    return p.getText();
                }
                throw new IOException("Not a String");
            }
        });
        mapper.registerModule(m);

        try {
            mapper.readValue("[\"a\", 3]", String[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }
    }