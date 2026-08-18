// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMapTest.java
public static class GenericHolder<T> {
        public T value;
    }
    public static class StringHolder extends GenericHolder<String> {
    }
    public static class TestClassNoTypeParams {
        public GenericHolder<String> holder;
    }

    public void testNoTypeParamsRefinement1384() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(GenericHolder.class, new JsonDeserializer<GenericHolder>() {
            @Override
            public GenericHolder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                StringHolder holder = new StringHolder();
                holder.value = "custom";
                return holder;
            }
        });
        mapper.registerModule(module);

        final String json = "{\"holder\":[\"" + StringHolder.class.getName() + "\",{}]}";
        TestClassNoTypeParams obj = mapper.readValue(json, TestClassNoTypeParams.class);
        assertNotNull(obj.holder);
        assertEquals("custom", obj.holder.value);
    }
