// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testHeaderWithQuotedValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("Name", "Value,With,Comma"));
        printer.printRecord("John", "test");
        assertEquals("Name,\"Value,With,Comma\"\r\nJohn,test\r\n", sw.toString());
        printer.close();
    }