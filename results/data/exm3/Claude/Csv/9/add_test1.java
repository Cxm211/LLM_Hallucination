// org/apache/commons/csv/CSVRecordTest.java
@Test
public void testToMapWithNoHeaderEmptyRecord() throws Exception {
    final CSVParser parser = CSVParser.parse("", CSVFormat.newFormat(','));
    if (parser.iterator().hasNext()) {
        final CSVRecord emptyRec = parser.iterator().next();
        Map<String, String> map = emptyRec.toMap();
        assertNotNull("Map is not null.", map);
        assertTrue("Map is empty.", map.isEmpty());
    }
}