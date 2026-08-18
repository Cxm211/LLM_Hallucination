// org/apache/commons/csv/CSVFormatTest.java
@Test
public void testNullRecordSeparatorPrintln() throws IOException {
    final CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(null);
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
        printer.print("value");
        printer.println();
        printer.print("value2");
    }
    final String result = sw.toString();
    assertNotNull(result);
    assertFalse(result.contains("null"));
    assertEquals("valuevalue2", result);
}