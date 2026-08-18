// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testStringArrayMultipleNulls() throws Exception
{
    String[] result = MAPPER.readValue("[null, null, null]", String[].class);
    assertNotNull(result);
    assertEquals(3, result.length);
    assertNull(result[0]);
    assertNull(result[1]);
    assertNull(result[2]);
}