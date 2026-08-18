// org/apache/commons/csv/CSVRecordTest.java
@Test
    public void testToMapWithEmptyRecord() throws Exception {
       final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
       final CSVRecord emptyRec = parser.iterator().next();
       final Map<String, String> map = emptyRec.toMap();
       assertTrue(map.isEmpty());
    }