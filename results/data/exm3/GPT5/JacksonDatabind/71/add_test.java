// com/fasterxml/jackson/databind/deser/TestMapDeserialization.java::testcharSequenceKeyMapIntValues
public void testcharSequenceKeyMapIntValues() throws Exception {
        String JSON = aposToQuotes("{'x':1,'y':2}");
        Map<CharSequence,Integer> result = MAPPER.readValue(JSON, new TypeReference<Map<CharSequence,Integer>>() { });
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
    }