// org/apache/commons/csv/CSVParserTest.java
@Test
public void testHeaderMixedEmptyNonEmpty() throws Exception {
    final Reader in = new StringReader("a,,b,,c\n1,2,3,4,5");
    CSVParser parser = CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(true).parse(in);
    Map<String, Integer> headerMap = parser.getHeaderMap();
    assertEquals(3, headerMap.size());
    assertTrue(headerMap.containsKey("a"));
    assertTrue(headerMap.containsKey("b"));
    assertTrue(headerMap.containsKey("c"));
}