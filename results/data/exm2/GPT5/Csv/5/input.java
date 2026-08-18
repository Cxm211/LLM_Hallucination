    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
            out.append(recordSeparator);
        newRecord = true;
    }

// trigger testcase
@Test
    public void testNullRecordSeparatorCsv106() {
        final CSVFormat format = CSVFormat.newFormat(';').withSkipHeaderRecord(true).withHeader("H1", "H2");
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }
