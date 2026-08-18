// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMapTest.java
public static class TestClassCollection {
        public List<String> listProperty;
    }

    public void testCollectionRefinement1384() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new JsonDeserializer<List>() {
            @Override
            public List deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return Arrays.asList("handler_used");
            }
        });
        mapper.registerModule(module);

        final String json = "{\"listProperty\":[\"" + ArrayList.class.getName() + "\",[]]}";
        TestClassCollection obj = mapper.readValue(json, TestClassCollection.class);
        assertEquals(1, obj.listProperty.size());
        assertEquals("handler_used", obj.listProperty.get(0));
    }
