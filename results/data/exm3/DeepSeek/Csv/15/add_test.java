// org/apache/commons/csv/CSVPrinterTest.java
@Test
    public void testUnicodeFirstCharNotQuoted() throws IOException {
        // Characters that should NOT be quoted when they are the first field
        // Euro, DEL, Greek alpha, Chinese ideograph
        final char[] unicodeChars = {'\u20AC', '\u007F', '\u03B1', '\u4E00'};
        for (char c : unicodeChars) {
            final StringWriter sw = new StringWriter();
            try (final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180)) {
                printer.printRecord(String.valueOf(c), "second");
                // In buggy version, the first field would be quoted, so assertion fails.
                assertEquals(String.valueOf(c) + ",second" + recordSeparator, sw.toString());
            }
        }
    }
