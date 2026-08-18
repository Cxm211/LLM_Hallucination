// com/fasterxml/jackson/databind/deser/exc/ExceptionPathTest.java
public void testReferenceChainForInnerClassWithIndex() throws Exception {
    class Inner {
        public List<Integer> list;
    }
    class Container {
        public Inner inner = new Inner();
    }
    String json = "{\"inner\":{\"list\":[\"not an int\"]}}";
    try {
        MAPPER.readValue(json, Container.class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        List<JsonMappingException.Reference> path = e.getPath();
        assertTrue("Path should have at least two references", path.size() >= 2);
        JsonMappingException.Reference indexRef = path.get(1);
        String expected = Inner.class.getName() + "[0]";
        assertEquals(expected, indexRef.toString());
    }
}
