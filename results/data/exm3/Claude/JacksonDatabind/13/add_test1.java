// com/fasterxml/jackson/databind/struct/TestObjectIdDeserialization.java
public void testNullObjectIdWithReference() throws Exception
{
    // Test null id with a forward reference scenario
    String json = aposToQuotes("{'value':7, 'id':null, 'next':{'value':8, 'id':1}}"     );
    Identifiable value = MAPPER.readValue(json, Identifiable.class);
    assertNotNull(value);
    assertEquals(7, value.value);
    assertNotNull(value.next);
    assertEquals(8, value.next.value);
}