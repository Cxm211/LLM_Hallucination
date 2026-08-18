// com/fasterxml/jackson/databind/deser/TestMapDeserialization.java
public void testCharSequenceKeyMapWithIntegerValues() throws Exception {
    String JSON = aposToQuotes("{'a':'b','c':123}");
    Map<CharSequence, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<CharSequence, Object>>() { });
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("b", result.get("a"));
    assertEquals(123, result.get("c"));
}
