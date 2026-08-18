public CSVPrinter(final Appendable out, final CSVFormat format) throws IOException {
        Assertions.notNull(out, "out");
        Assertions.notNull(format, "format");

        this.out = out;
        this.format = format;
        this.format.validate();
        // Print header immediately if present and not skipped
        final String[] header = this.format.getHeader();
        if (header != null && !this.format.getSkipHeaderRecord()) {
            printRecord((Object[]) header);
        }
    }