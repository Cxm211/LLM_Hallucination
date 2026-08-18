// com/fasterxml/jackson/databind/introspect/TypeCoercion1592Test.java
public void testTypeCoercionWithPrimitiveWrapperInCollectionContent() throws Exception {
    class BeanWithCollection {
        @JsonSerialize(contentAs = Long.class)
        public List<Long> values = Arrays.asList(1L, 2L, 3L);
    }
    String json = MAPPER.writeValueAsString(new BeanWithCollection());
    assertTrue(json.contains("values"));
}