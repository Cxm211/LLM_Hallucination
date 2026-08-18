// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testDontQuoteUnicodeCharactersInMiddle() throws IOException {
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
        printer.printRecord("Hello", "café", "世界");
        assertEquals("Hello,café,世界" + recordSeparator, sw.toString());
    }
}