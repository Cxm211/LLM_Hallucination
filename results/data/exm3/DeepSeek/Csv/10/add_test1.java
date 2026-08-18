// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testHeaderWithQuote() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("E", "F\"G", "H"));
        printer.printRecord("4", "5", "6");
        assertEquals("E,\"F\"\"G\",H\r\n4,5,6\r\n", sw.toString());
        printer.close();
    }
