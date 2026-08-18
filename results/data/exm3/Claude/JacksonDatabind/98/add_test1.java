// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdWithEnum1328Test.java
public void testEnumTypeIdSingleObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        AnimalAndType input = new AnimalAndType(AnimalType.Cat, new Cat());
        
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);
        AnimalAndType result = mapper.readerFor(AnimalAndType.class)
            .readValue(json);
        
        assertNotNull(result);
        assertEquals(AnimalType.Cat, result.type);
        assertNotNull(result.animal);
    }