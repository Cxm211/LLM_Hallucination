// org/apache/commons/csv/CSVRecordTest.java
@Test
    public void testToMapWithSingleColumnShort() throws Exception {
       final CSVParser parser = CSVParser.parse("value1", CSVFormat.DEFAULT.withHeader("A", "B"));
       final CSVRecord shortRec = parser.iterator().next();
       final Map<String, String> map = shortRec.toMap();
       assertEquals(1, map.size());
       assertEquals("value1", map.get("A"));
       assertFalse(map.containsKey("B"));
    }