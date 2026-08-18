// org/apache/commons/csv/CSVFormatTest.java
@Test
    public void testPrintlnWithNullRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(null);
        StringWriter out = new StringWriter();
        CSVPrinter printer = new CSVPrinter(out, format);
        printer.println();
        String output = out.toString();
        assertFalse(output.contains("null"));
    }
