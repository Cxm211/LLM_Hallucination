// org/apache/commons/csv/CSVRecordTest.java
@Test
public void testToMapWithNoHeaderMultipleRecords() throws Exception {
    final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.newFormat(','));
    final Iterator<CSVRecord> iterator = parser.iterator();
    
    final CSVRecord firstRec = iterator.next();
    Map<String, String> map1 = firstRec.toMap();
    assertNotNull("First map is not null.", map1);
    assertTrue("First map is empty.", map1.isEmpty());
    
    final CSVRecord secondRec = iterator.next();
    Map<String, String> map2 = secondRec.toMap();
    assertNotNull("Second map is not null.", map2);
    assertTrue("Second map is empty.", map2.isEmpty());
}