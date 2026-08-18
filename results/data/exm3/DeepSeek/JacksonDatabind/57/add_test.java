// com/fasterxml/jackson/databind/seq/ReadValuesTest.java
public void testReadValuesWithNonZeroOffset() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(Integer.class);
    byte[] data = "X[1,2,3]".getBytes("UTF-8");
    int offset = 1;
    int length = data.length - offset;
    MappingIterator<Integer> it = reader.readValues(data, offset, length);
    assertTrue(it.hasNext());
    assertEquals(Integer.valueOf(1), it.next());
    assertEquals(Integer.valueOf(2), it.next());
    assertEquals(Integer.valueOf(3), it.next());
    assertFalse(it.hasNext());
}
