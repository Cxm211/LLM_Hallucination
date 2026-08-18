// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testStringArrayMixedNullsAndStrings() throws Exception
{
    String[] result = MAPPER.readValue("[\"first\", null, \"middle\", null, \"last\"]", String[].class);
    assertNotNull(result);
    assertEquals(5, result.length);
    assertEquals("first", result[0]);
    assertNull(result[1]);
    assertEquals("middle", result[2]);
    assertNull(result[3]);
    assertEquals("last", result[4]);
}