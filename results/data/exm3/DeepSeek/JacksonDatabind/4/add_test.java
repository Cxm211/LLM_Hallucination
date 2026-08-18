// com/fasterxml/jackson/databind/deser/TestCollectionDeserialization.java
public void testArrayIndexForExceptionsAdditional() throws Exception
{
    // Test error on first element
    try {
        MAPPER.readValue("[ false ]", String[].class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "Can not deserialize");
        List<JsonMappingException.Reference> refs = e.getPath();
        assertEquals(1, refs.size());
        assertEquals(0, refs.get(0).getIndex());
    }

    // Test error on third element, assuming chunk size might cause reset
    try {
        MAPPER.readValue("[ \"a\", \"b\", { } ]", String[].class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "Can not deserialize");
        List<JsonMappingException.Reference> refs = e.getPath();
        assertEquals(1, refs.size());
        assertEquals(2, refs.get(0).getIndex());
    }

    // Test for custom deserializer, e.g., Key[].class with error on first element
    try {
        MAPPER.readValue("[ false ]", Key[].class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "Can not deserialize");
        List<JsonMappingException.Reference> refs = e.getPath();
        assertEquals(1, refs.size());
        assertEquals(0, refs.get(0).getIndex());
    }
}
