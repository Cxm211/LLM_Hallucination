// com/fasterxml/jackson/databind/seq/ReadValuesTest.java
public void testRootBeansWithOffset() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json1 = "{\"a\":1}";
    String json2 = "{\"a\":2}";
    String json3 = "{\"a\":3}";
    String combined = json1 + "\n" + json2 + "\n" + json3;
    byte[] data = combined.getBytes("UTF-8");
    
    // Test with offset pointing to second JSON object
    int offset = json1.length() + 1; // skip first object and newline
    int length = json2.length() + 1 + json3.length(); // read second and third
    
    MappingIterator<Map> it = mapper.readerFor(Map.class).readValues(data, offset, length);
    assertTrue(it.hasNext());
    Map first = it.next();
    assertEquals(Integer.valueOf(2), first.get("a"));
    assertTrue(it.hasNext());
    Map second = it.next();
    assertEquals(Integer.valueOf(3), second.get("a"));
    assertFalse(it.hasNext());
    it.close();
}