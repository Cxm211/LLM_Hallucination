// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testNullWithEscape() throws IOException {
        Object[] s = new Object[] { null };
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        String expected = "NULL\n";
        assertEquals(expected, writer.toString());
    }
