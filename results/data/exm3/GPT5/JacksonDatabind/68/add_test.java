// com/fasterxml/jackson/databind/struct/SingleValueAsArrayTest.java::testSuccessfulDeserializationOfObjectWithChainedArrayCreators
public void testDelegatingFromListWithArrayInput() throws Exception {
        ObjectMapper m = new ObjectMapper();
        BeanListDelegating b = m.readValue("[\"a\",\"b\"]", BeanListDelegating.class);
        assertNotNull(b);
        assertEquals(Arrays.asList("a","b"), b.values);
    }

    static class BeanListDelegating {
        public final List<String> values;
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public BeanListDelegating(List<String> v) { this.values = v; }
    }