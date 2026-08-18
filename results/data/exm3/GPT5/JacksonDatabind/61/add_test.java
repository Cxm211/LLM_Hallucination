// com/fasterxml/jackson/databind/jsontype/DefaultTypingWithPrimitivesTest.java::testDefaultTypingWithLong
public void testDefaultTypingWithPrimitiveLongField() throws Exception
    {
        class P { public long v = 3L; }
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        String json = mapper.writeValueAsString(new P());
        P back = mapper.readValue(json, P.class);
        assertEquals(3L, back.v);
    }