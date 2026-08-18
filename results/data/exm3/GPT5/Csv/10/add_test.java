// org/apache/commons/csv/CSVPrinterTest.java::testHeaderPrintedInConstructor
@Test
    public void testHeaderPrintedInConstructor() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null)
                .withHeader("C1", "C2", "C3"));
        assertEquals("C1,C2,C3\r\n", sw.toString());
        printer.close();
    }