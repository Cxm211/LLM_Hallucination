// org/apache/commons/csv/CSVParserTest.java
@Test
public void testHeaderMapImmutability() throws Exception {
    final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT.withFirstRecordAsHeader());
    final Map<String, Integer> headerMap1 = parser.getHeaderMap();
    final Map<String, Integer> headerMap2 = parser.getHeaderMap();
    Assert.assertNotSame(headerMap1, headerMap2);
    headerMap1.put("d", 3);
    Assert.assertFalse(parser.getHeaderMap().containsKey("d"));
}