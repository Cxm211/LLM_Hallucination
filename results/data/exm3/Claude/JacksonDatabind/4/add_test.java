// com/fasterxml/jackson/databind/deser/TestCollectionDeserialization.java
public void testArrayIndexForExceptionsAtIndex0() throws Exception
{
    try {
        MAPPER.readValue("[ { } ]", String[].class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        verifyException(e, "Can not deserialize");
        List<JsonMappingException.Reference> refs = e.getPath();
        assertEquals(1, refs.size());
        assertEquals(0, refs.get(0).getIndex());
    }
}