    public CSVPrinter(final Appendable out, final CSVFormat format) throws IOException {
        Assertions.notNull(out, "out");
        Assertions.notNull(format, "format");

        this.out = out;
        this.format = format;
        this.format.validate();
        // TODO: Is it a good idea to do this here instead of on the first call to a print method?
        // It seems a pain to have to track whether the header has already been printed or not.
    }

// trigger testcase
@Test
    public void testHeader() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null)
                .withHeader("C1", "C2", "C3"));
        printer.printRecord("a", "b", "c");
        printer.printRecord("x", "y", "z");
        assertEquals("C1,C2,C3\r\na,b,c\r\nx,y,z\r\n", sw.toString());
        printer.close();
    }
