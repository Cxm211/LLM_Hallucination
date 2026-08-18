// com/fasterxml/jackson/databind/objectid/TestObjectIdSerialization.java::testMissingStringPropertyId
public void testMissingStringPropertyId() throws Exception
    {
        IdentifiableStringId value = MAPPER.readValue
                (aposToQuotes("{'value':3, 'next':null}"), IdentifiableStringId.class);
        assertNotNull(value);
        assertEquals(3, value.value);
    }