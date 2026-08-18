// org/apache/commons/csv/CSVRecordTest.java
@Test
public void testToMapWithShortRecordNullForMissing() throws Exception {
   final CSVParser parser =  CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
   final CSVRecord shortRec = parser.iterator().next();
   final Map<String, String> map = shortRec.toMap();
   assertTrue(map.containsKey("C"));
   assertNull(map.get("C"));
}