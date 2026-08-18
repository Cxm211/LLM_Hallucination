// com/fasterxml/jackson/databind/deser/TestMapDeserialization.java
public void testEmptyCharSequenceKeyMap() throws Exception {
    String JSON = "{}";
    Map<CharSequence,String> result = MAPPER.readValue(JSON, new TypeReference<Map<CharSequence,String>>() { });
    assertNotNull(result);
    assertEquals(0, result.size());
}