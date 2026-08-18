// com/fasterxml/jackson/databind/introspect/TypeCoercion1592Test.java
public void testTypeCoercionWithPrimitiveWrapperInMapKey() throws Exception {
    class BeanWithMapKey {
        @JsonSerialize(keyAs = Integer.class)
        public Map<Integer, String> map = new HashMap<>();
        BeanWithMapKey() {
            map.put(1, "value");
        }
    }
    String json = MAPPER.writeValueAsString(new BeanWithMapKey());
    assertTrue(json.contains("map"));
}