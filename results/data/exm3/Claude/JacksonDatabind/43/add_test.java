// com/fasterxml/jackson/databind/objectid/TestObjectIdSerialization.java
public void testNullNumericPropertyId() throws Exception
{
    IdentifiableNumericId value = MAPPER.readValue
            (aposToQuotes("{'value':5, 'next':null, 'id':null}"), IdentifiableNumericId.class);
    assertNotNull(value);
    assertEquals(5, value.value);
}