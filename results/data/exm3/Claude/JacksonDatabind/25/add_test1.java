// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testByteArrayContentDeserializerWithEmptyString() throws Exception
    {
        // Test when content deserializer processes empty base64 string
        HiddenBinaryBean890 result = MAPPER.readValue(
                aposToQuotes("{'someBytes':''}"), HiddenBinaryBean890.class);
        assertNotNull(result);
        assertNotNull(result.someBytes);
        assertEquals(0, result.someBytes.length);
    }