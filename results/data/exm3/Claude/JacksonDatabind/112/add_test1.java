// com/fasterxml/jackson/databind/deser/creators/DelegatingArrayCreator2324Test.java
public void testDeserializeEmptyBagWithArrayDelegate() throws Exception {
        WithBagOfStringsArrayDelegate result = MAPPER.readerFor(WithBagOfStringsArrayDelegate.class)
                .readValue("{\"strings\": []}");
        assertEquals(0, result.getStrings().size());
    }