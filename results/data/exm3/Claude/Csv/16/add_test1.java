// org/apache/commons/csv/CSVParserTest.java
@Test
public void testIteratorNextWithoutHasNext() throws IOException {
    final String twoRows = "a\nb\n";
    CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(twoRows));
    Iterator<CSVRecord> iter = parser.iterator();
    
    CSVRecord record1 = iter.next();
    assertEquals("a", record1.get(0));
    
    CSVRecord record2 = iter.next();
    assertEquals("b", record2.get(0));
    
    try {
        iter.next();
        fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
        // Expected
    }
}