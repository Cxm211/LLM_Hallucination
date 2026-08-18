// buggy function
    private void print(final Object object, final CharSequence value, final int offset, final int len)
            throws IOException {
        if (!newRecord) {
            out.append(format.getDelimiter());
        }
        if (format.isQuoteCharacterSet()) {
            // the original object is needed so can check for Number
            printAndQuote(object, value, offset, len);
        } else if (format.isEscapeCharacterSet()) {
            printAndEscape(value, offset, len);
        } else {
            out.append(value, offset, offset + len);
        }
        newRecord = false;
    }

// trigger testcase
// org/apache/commons/csv/CSVPrinterTest.java::testMySqlNullOutput
@Test
    public void testMySqlNullOutput() throws IOException {
        Object[] s = new String[] { "NULL", null };
        CSVFormat format = CSVFormat.MYSQL.withQuote('"').withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        String expected = "\"NULL\"\tNULL\n";
        assertEquals(expected, writer.toString());
        String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(new Object[2], record0);

        s = new String[] { "\\N", null };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\n", "A" };
        format = CSVFormat.MYSQL.withNullString("\\N");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\n\tA\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL.withNullString("NULL");
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\tNULL\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "", null };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\t\\N\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\N", "", "\u000e,\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\N\t\t\u000e,\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "NULL", "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "NULL\t\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);

        s = new String[] { "\\\r" };
        format = CSVFormat.MYSQL;
        writer = new StringWriter();
        printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        printer.close();
        expected = "\\\\\\r\n";
        assertEquals(expected, writer.toString());
        record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(s, format), record0);
    }

// org/apache/commons/csv/CSVPrinterTest.java::testMySqlNullStringDefault
@Test
    public void testMySqlNullStringDefault() throws IOException {
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }
