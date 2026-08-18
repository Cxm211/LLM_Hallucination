// org/apache/commons/csv/CSVPrinterTest.java::testNullValueNoQuoteNoEscape
@Test
public void testNullValueNoQuoteNoEscape() throws IOException {
    CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape(null).withNullString("NULL").withDelimiter(',');
    StringWriter writer = new StringWriter();
    CSVPrinter printer = new CSVPrinter(writer, format);
    printer.printRecord("A", null, "B");
    printer.close();
    assertEquals("A,NULL,B\n", writer.toString());
    String[] record0 = toFirstRecordValues(writer.toString(), format);
    assertArrayEquals(expectNulls(new String[] { "A", null, "B" }, format), record0);
}