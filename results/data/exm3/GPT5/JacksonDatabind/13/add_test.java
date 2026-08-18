// com/fasterxml/jackson/databind/struct/TestObjectIdDeserialization.java::testNullObjectIdNoOtherFields
public void testNullObjectIdNoOtherFields() throws Exception
    {
        // Only id present and it is null; should not crash and id should remain null
        Identifiable value = MAPPER.readValue(aposToQuotes("{'id':null}"), Identifiable.class);
        assertNotNull(value);
        assertNull(value.id);
    }