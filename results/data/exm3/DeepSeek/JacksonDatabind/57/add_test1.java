// com/fasterxml/jackson/databind/seq/ReadValuesTest.java
public void testReadValuesWithLengthLessThanArray() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(Integer.class);
    byte[] data = "[1,2,3]extra".getBytes("UTF-8");
    byte[] validPart = "[1,2,3]".getBytes("UTF-8");
    int offset = 0;
    int length = validPart.length;
    MappingIterator<Integer> it = reader.readValues(data, offset, length);
    assertTrue(it.hasNext());
    assertEquals(Integer.valueOf(1), it.next());
    assertEquals(Integer.valueOf(2), it.next());
    assertEquals(Integer.valueOf(3), it.next());
    assertFalse(it.hasNext());
}
