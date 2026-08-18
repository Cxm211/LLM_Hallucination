// org/apache/commons/csv/CSVParserTest.java
@Test
public void testIteratorMultipleHasNextCalls() throws IOException {
    final String threeRows = "1\n2\n3\n";
    CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(threeRows));
    Iterator<CSVRecord> iter = parser.iterator();
    
    assertTrue(iter.hasNext());
    assertTrue(iter.hasNext());
    assertTrue(iter.hasNext());
    CSVRecord record = iter.next();
    assertEquals("1", record.get(0));
    
    assertTrue(iter.hasNext());
    assertTrue(iter.hasNext());
    record = iter.next();
    assertEquals("2", record.get(0));
    
    assertTrue(iter.hasNext());
    record = iter.next();
    assertEquals("3", record.get(0));
    
    assertFalse(iter.hasNext());
    assertFalse(iter.hasNext());
}