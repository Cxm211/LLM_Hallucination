// com/fasterxml/jackson/databind/jsontype/TestTypeNames.java
public void testCollectSubtypesByClassWithNullProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Collection<NamedType> subtypes = new StdSubtypeResolver().collectAndResolveSubtypesByClass(
                mapper.getDeserializationConfig(),
                null,
                mapper.constructType(Object.class));
        assertTrue(subtypes.isEmpty());
    }
