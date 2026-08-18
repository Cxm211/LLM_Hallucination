// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testHeaderWithComma() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("A", "B,C", "D"));
        printer.printRecord("1", "2", "3");
        assertEquals("A,\"B,C\",D\r\n1,2,3\r\n", sw.toString());
        printer.close();
    }
