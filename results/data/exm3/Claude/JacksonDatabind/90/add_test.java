// com/fasterxml/jackson/databind/creators/DelegatingArrayCreator1804Test.java
public void testDelegatingArrayWithValues1804() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    MyType thing = mapper.readValue("[1, 2, 3]", MyType.class);
    assertNotNull(thing);
}