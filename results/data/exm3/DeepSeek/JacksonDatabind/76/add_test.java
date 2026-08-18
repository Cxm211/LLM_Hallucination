// com/fasterxml/jackson/databind/deser/builder/BuilderWithUnwrappedTest.java
public void testWithUnwrappedAndCreatorMultipleParametersWithUnwrappedAfter() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','years_old':30,'animal_id':1234,'living':true,'last_name':'Doe'}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }
