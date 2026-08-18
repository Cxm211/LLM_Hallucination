// com/fasterxml/jackson/databind/deser/TestJDKAtomicTypes.java
public void testNonAbsentTypeRefinement() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    class Bean {
        private java.util.concurrent.atomic.AtomicReference<String> value = new java.util.concurrent.atomic.AtomicReference<>();
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(as = java.util.concurrent.atomic.AtomicReference.class)
        public Object getValue() { return value; }
    }
    String json = mapper.writeValueAsString(new Bean());
    assertEquals("{}", json);
}
