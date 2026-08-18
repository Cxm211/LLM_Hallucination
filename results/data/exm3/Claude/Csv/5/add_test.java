// org/apache/commons/csv/CSVFormatTest.java
@Test
public void testNullRecordSeparatorWithMultipleRecords() throws IOException {
    final CSVFormat format = CSVFormat.newFormat(';').withRecordSeparator(null);
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
        printer.printRecord("A", "B");
        printer.printRecord("C", "D");
    }
    final String result = sw.toString();
    assertNotNull(result);
    assertFalse(result.contains("null"));
    assertTrue(result.contains("A;B"));
    assertTrue(result.contains("C;D"));
}