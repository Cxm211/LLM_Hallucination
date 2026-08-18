// com/fasterxml/jackson/databind/deser/filter/ProblemHandlerUnknownTypeId2221Test.java
public void testHandlerReturnsNull() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .enableDefaultTyping();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId, TypeIdResolver idResolver, String failureMsg) throws IOException {
                return null;
            }
        });
        GenericContent processableContent = mapper.readValue(JSON, GenericContent.class);
        assertNotNull(processableContent.getInnerObjects());
        assertEquals(2, processableContent.getInnerObjects().size());
        for (Object obj : processableContent.getInnerObjects()) {
            assertNull(obj);
        }
    }