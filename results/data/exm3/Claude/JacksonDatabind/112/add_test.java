// com/fasterxml/jackson/databind/deser/creators/DelegatingArrayCreator2324Test.java
public void testDeserializeBagOfStringsWithArrayDelegate() throws Exception {
        WithBagOfStringsArrayDelegate result = MAPPER.readerFor(WithBagOfStringsArrayDelegate.class)
                .readValue("{\"strings\": [ \"x\", \"y\" ]}");
        assertEquals(2, result.getStrings().size());
        assertTrue(result.getStrings().contains("x"));
        assertTrue(result.getStrings().contains("y"));
    }