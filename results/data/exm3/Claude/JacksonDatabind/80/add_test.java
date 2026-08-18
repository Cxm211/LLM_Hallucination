// com/fasterxml/jackson/databind/jsontype/TestTypeNames.java
public void testBaseTypeIdWithNullPropertyClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Collection<NamedType> subtypes = new StdSubtypeResolver().collectAndResolveSubtypesByClass(
                mapper.getDeserializationConfig(),
                null,
                mapper.constructType(Base1616.class));
        assertEquals(2, subtypes.size());
    }