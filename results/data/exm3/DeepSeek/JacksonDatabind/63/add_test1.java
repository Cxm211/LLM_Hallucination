// com/fasterxml/jackson/databind/deser/exc/ExceptionPathTest.java
public void testReferenceChainForInnerClassRoot() throws Exception {
    class MyInner {
        public int value;
    }
    String json = "\"not an object\"";
    try {
        MAPPER.readValue(json, MyInner.class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        List<JsonMappingException.Reference> path = e.getPath();
        assertFalse("Path should not be empty", path.isEmpty());
        JsonMappingException.Reference rootRef = path.get(0);
        String expected = MyInner.class.getName() + "[?]";
        assertEquals(expected, rootRef.toString());
    }
}
