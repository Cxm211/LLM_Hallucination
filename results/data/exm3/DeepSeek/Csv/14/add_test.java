// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testMinimalQuotePeriod() throws IOException {
        StringWriter sw = new StringWriter();
        try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print(".");
        }
        assertEquals(".", sw.toString());
    }
