// com/fasterxml/jackson/databind/deser/TestJDKAtomicTypes.java
public void testAlwaysContainerTypeRefinement() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    class Bean {
        private java.util.ArrayList<String> value = new java.util.ArrayList<>();
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(as = java.util.ArrayList.class)
        public Object getValue() { return value; }
    }
    String json = mapper.writeValueAsString(new Bean());
    assertEquals("{}", json);
}
