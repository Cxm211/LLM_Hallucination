// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testByteArrayContentDeserializerWithNull() throws Exception
    {
        // Test when content deserializer definition is null
        HiddenBinaryBean890 result = MAPPER.readValue(
                aposToQuotes("{'someBytes':null}"), HiddenBinaryBean890.class);
        assertNotNull(result);
        assertNull(result.someBytes);
    }