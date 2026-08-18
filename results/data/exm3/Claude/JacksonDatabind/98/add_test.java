// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdWithEnum1328Test.java
public void testEnumTypeIdWithMultipleItems() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        List<AnimalAndType> input = Arrays.asList(
            new AnimalAndType(AnimalType.Dog, new Dog()),
            new AnimalAndType(AnimalType.Cat, new Cat()),
            new AnimalAndType(AnimalType.Dog, new Dog())
        );
        
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);
        List<AnimalAndType> list = mapper.readerFor(new TypeReference<List<AnimalAndType>>() { })
            .readValue(json);
        
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(AnimalType.Dog, list.get(0).type);
        assertEquals(AnimalType.Cat, list.get(1).type);
        assertEquals(AnimalType.Dog, list.get(2).type);
    }