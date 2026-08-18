// org/apache/commons/csv/CSVRecordTest.java
@Test
    public void testToMapWithMultipleColumnsShort() throws Exception {
       final CSVParser parser = CSVParser.parse("v1,v2", CSVFormat.DEFAULT.withHeader("A", "B", "C", "D"));
       final CSVRecord shortRec = parser.iterator().next();
       final Map<String, String> map = shortRec.toMap();
       assertEquals(2, map.size());
       assertEquals("v1", map.get("A"));
       assertEquals("v2", map.get("B"));
       assertFalse(map.containsKey("C"));
       assertFalse(map.containsKey("D"));
    }