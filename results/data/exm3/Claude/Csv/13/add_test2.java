// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testNullVsNullStringLiteral() throws IOException {
    Object[] s = new Object[] { null, "NULL", Integer.valueOf(5) };
    CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC);
    StringWriter writer = new StringWriter();
    CSVPrinter printer = new CSVPrinter(writer, format);
    printer.printRecord(s);
    printer.close();
    String expected = "NULL,\"NULL\",5\n";
    assertEquals(expected, writer.toString());
}