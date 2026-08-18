// org/apache/commons/csv/CSVParserTest.java
@Test
public void testIteratorMixedHasNextAndNext() throws IOException {
    final String fourRows = "x\ny\nz\nw\n";
    CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(fourRows));
    Iterator<CSVRecord> iter = parser.iterator();
    
    assertTrue(iter.hasNext());
    CSVRecord record1 = iter.next();
    assertEquals("x", record1.get(0));
    
    CSVRecord record2 = iter.next();
    assertEquals("y", record2.get(0));
    
    assertTrue(iter.hasNext());
    CSVRecord record3 = iter.next();
    assertEquals("z", record3.get(0));
    
    assertTrue(iter.hasNext());
    assertTrue(iter.hasNext());
    CSVRecord record4 = iter.next();
    assertEquals("w", record4.get(0));
    
    assertFalse(iter.hasNext());
}