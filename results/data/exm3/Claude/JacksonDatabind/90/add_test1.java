// com/fasterxml/jackson/databind/creators/DelegatingArrayCreator1804Test.java
public void testDelegatingArrayNull1804() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    MyType thing = mapper.readValue("null", MyType.class);
    assertNull(thing);
}