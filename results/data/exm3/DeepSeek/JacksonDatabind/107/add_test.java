// com/fasterxml/jackson/databind/deser/filter/ProblemHandlerUnknownTypeId2221Test.java
public void testKnownTypeId() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .enableDefaultTyping();
        String json = "[\"java.lang.String\", \"test\"]";
        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof String);
        assertEquals("test", result);
    }
