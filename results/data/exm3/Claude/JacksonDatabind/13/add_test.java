// com/fasterxml/jackson/databind/struct/TestObjectIdDeserialization.java
public void testNullObjectIdWithoutIdProperty() throws Exception
{
    // Test null id when idProperty is not set
    String json = aposToQuotes("{'value':5, 'next':null, 'id':null}");
    Identifiable value = MAPPER.readValue(json, Identifiable.class);
    assertNotNull(value);
    assertEquals(5, value.value);
    assertNull(value.next);
}