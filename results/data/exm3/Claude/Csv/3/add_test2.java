// org/apache/commons/csv/CSVParserTest.java
@Test
public void testBackslashEscapingWithUnknownChar() throws IOException {
    final String code = "value\\xtest";
    final CSVFormat format = CSVFormat.newBuilder(',').withEscape('\\').build();
    final CSVParser parser = new CSVParser(code, format);
    final List<CSVRecord> records = parser.getRecords();
    assertEquals(1, records.size());
    assertEquals("value\\xtest", records.get(0).get(0));
}