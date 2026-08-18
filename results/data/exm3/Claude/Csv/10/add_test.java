// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testHeaderWithSingleColumn() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("Column1"));
        printer.printRecord("value1");
        assertEquals("Column1\r\nvalue1\r\n", sw.toString());
        printer.close();
    }