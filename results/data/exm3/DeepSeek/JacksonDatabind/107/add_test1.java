// com/fasterxml/jackson/databind/deser/filter/ProblemHandlerUnknownTypeId2221Test.java
public void testUnknownTypeIdNoHandler() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .enableDefaultTyping();
        String json = "[\"unknown.Type\", {}]";
        try {
            mapper.readValue(json, Object.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // success
        }
    }
