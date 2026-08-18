// org/apache/commons/csv/CSVParserTest.java
public void testDuplicateHeaderEntriesExplicit() throws Exception {
    CSVParser parser = CSVParser.parse("1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("a", "b", "a"));
    Map<String, Integer> headerMap = parser.getHeaderMap();
    assertEquals(Integer.valueOf(0), headerMap.get("a"));
    assertEquals(Integer.valueOf(1), headerMap.get("b"));
    assertEquals(2, headerMap.size());
}
