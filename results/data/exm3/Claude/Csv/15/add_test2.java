// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testQuoteUnicodeWithComma() throws IOException {
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
        printer.printRecord("€100,50", "data");
        assertEquals("\"€100,50\",data" + recordSeparator, sw.toString());
    }
}