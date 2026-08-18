// org/apache/commons/csv/CSVPrinterTest.java
@Test
public void testDontQuoteCJKFirstChar() throws IOException {
    final StringWriter sw = new StringWriter();
    try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
        printer.printRecord("你好", "world");
        assertEquals("你好,world" + recordSeparator, sw.toString());
    }
}