// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testQuoteCharInMiddle() throws IOException {
    StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuote(QUOTE_CH))) {
        printer.print("a\"b");
    }
    assertEquals("\"a\"\"b\"", sw.toString());
}