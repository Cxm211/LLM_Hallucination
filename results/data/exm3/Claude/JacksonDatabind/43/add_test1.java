// com/fasterxml/jackson/databind/objectid/TestObjectIdSerialization.java
public void testValidPropertyIdAfterNull() throws Exception
{
    IdentifiableStringId value = MAPPER.readValue
            (aposToQuotes("{'value':7, 'next':null, 'id':'valid-id'}"), IdentifiableStringId.class);
    assertNotNull(value);
    assertEquals(7, value.value);
    assertEquals("valid-id", value.id);
}