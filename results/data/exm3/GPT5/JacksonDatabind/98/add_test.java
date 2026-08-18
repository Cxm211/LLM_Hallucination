// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdWithEnum1328Test.java::testSingleValue
public void testSingleValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new AnimalAndType(AnimalType.Cat, new Dog()));
        AnimalAndType result = mapper.readValue(json, AnimalAndType.class);
        assertNotNull(result);
        assertEquals(AnimalType.Cat, result.type);
        assertNotNull(result.animal);
    }