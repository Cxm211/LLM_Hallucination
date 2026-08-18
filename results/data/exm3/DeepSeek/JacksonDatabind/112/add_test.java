// com/fasterxml/jackson/databind/deser/creators/DelegatingArrayCreator2324Test.java
public void testDeserializeBagOfIntegers() throws Exception {
        WithBagOfIntegers result = MAPPER.readerFor(WithBagOfIntegers.class)
                .readValue("{\"integers\": [ 1, 2, 3]}");
        assertEquals(3, result.getIntegers().size());
    }
