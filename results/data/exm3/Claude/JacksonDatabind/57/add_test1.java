// com/fasterxml/jackson/databind/seq/ReadValuesTest.java
public void testRootBeansWithSubarray() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String prefix = "xxxxx";
    String json = "{\"value\":42}";
    String suffix = "yyyyy";
    String combined = prefix + json + suffix;
    byte[] data = combined.getBytes("UTF-8");
    
    // Test reading only the middle portion containing valid JSON
    int offset = prefix.length();
    int length = json.length();
    
    MappingIterator<Map> it = mapper.readerFor(Map.class).readValues(data, offset, length);
    assertTrue(it.hasNext());
    Map result = it.next();
    assertEquals(Integer.valueOf(42), result.get("value"));
    assertFalse(it.hasNext());
    it.close();
}