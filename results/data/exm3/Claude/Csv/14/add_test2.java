// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testQuoteCharAtEnd() throws IOException {
    StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
        printer.print("test\"");
    }
    assertEquals("\"test\"\"\"", sw.toString());
}