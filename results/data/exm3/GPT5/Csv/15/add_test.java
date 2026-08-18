// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testDontQuoteEmojiFirstChar() throws IOException {
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
        printer.printRecord("😀", "Deux");
        assertEquals("😀,Deux" + recordSeparator, sw.toString());
    }
}
