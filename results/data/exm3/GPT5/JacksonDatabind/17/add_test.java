// com/fasterxml/jackson/databind/node/TestJsonNode.java::testArrayWithDefaultTypingNonConcreteAndArrays
public void testArrayWithDefaultTypingNonConcreteAndArrays() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

        JsonNode array = mapper.readTree("[ 1, 2 ]");
        assertTrue(array.isArray());
        assertEquals(2, array.size());

        JsonNode obj = mapper.readTree("{ \"a\" : 2 }");
        assertTrue(obj.isObject());
        assertEquals(1, obj.size());
        assertEquals(2, obj.path("a").asInt());
    }