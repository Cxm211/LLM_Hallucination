// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testNullStringWithQuoteModeAll() throws IOException {
    Object[] s = new Object[] { null, "data", null };
    CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.ALL);
    StringWriter writer = new StringWriter();
    CSVPrinter printer = new CSVPrinter(writer, format);
    printer.printRecord(s);
    printer.close();
    String expected = "NULL,\"data\",NULL\n";
    assertEquals(expected, writer.toString());
}