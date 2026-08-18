// com/fasterxml/jackson/databind/creators/DelegatingArrayCreator1804Test.java
public void testDelegatingArrayNonEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MyType thing = mapper.readValue("[\"value\"]", MyType.class);
        assertNotNull(thing);
    }
