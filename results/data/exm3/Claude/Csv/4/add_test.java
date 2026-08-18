// org/apache/commons/csv/CSVParserTest.java
@Test
public void testHeaderMapWithHeaders() throws Exception {
    final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withFirstRecordAsHeader());
    final Map<String, Integer> headerMap = parser.getHeaderMap();
    Assert.assertNotNull(headerMap);
    Assert.assertEquals(3, headerMap.size());
    Assert.assertEquals(Integer.valueOf(0), headerMap.get("a"));
    Assert.assertEquals(Integer.valueOf(1), headerMap.get("b"));
    Assert.assertEquals(Integer.valueOf(2), headerMap.get("c"));
}