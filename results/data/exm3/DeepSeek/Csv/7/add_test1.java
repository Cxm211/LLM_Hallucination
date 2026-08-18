// org/apache/commons/csv/CSVParserTest.java
public void testDuplicateHeaderEntriesExplicitSkip() throws Exception {
    CSVParser parser = CSVParser.parse("skip this\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("a", "b", "a").withSkipHeaderRecord(true));
    Map<String, Integer> headerMap = parser.getHeaderMap();
    assertEquals(Integer.valueOf(0), headerMap.get("a"));
    assertEquals(Integer.valueOf(1), headerMap.get("b"));
    assertEquals(2, headerMap.size());
}
