// com/fasterxml/jackson/databind/deser/TestMapDeserialization.java
public void testObjectKeyMap() throws Exception {
    String JSON = aposToQuotes("{'key1':'value1','key2':'value2'}");
    Map<Object,String> result = MAPPER.readValue(JSON, new TypeReference<Map<Object,String>>() { });
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("value1", result.get("key1"));
    assertEquals("value2", result.get("key2"));
}