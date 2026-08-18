// com/fasterxml/jackson/databind/deser/TestCollectionDeserialization.java
public void testArrayIndexForExceptionsAtIndex2() throws Exception
{
    try {
        MAPPER.readValue("[ \"valid1\", \"valid2\", { } ]", String[].class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "Can not deserialize");
        List<JsonMappingException.Reference> refs = e.getPath();
        assertEquals(1, refs.size());
        assertEquals(2, refs.get(0).getIndex());
    }
}