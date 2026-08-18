// com/fasterxml/jackson/databind/objectid/TestObjectIdSerialization.java
public void testNullIntPropertyId() throws Exception
    {
        IdentifiableIntId value = MAPPER.readValue
                (aposToQuotes("{'value':5, 'next':null, 'id':null}"), IdentifiableIntId.class);
        assertNotNull(value);
        assertEquals(5, value.value);
    }
