public CSVPrinter(final Appendable out, final CSVFormat format) throws IOException {
        Assertions.notNull(out, "out");
        Assertions.notNull(format, "format");

        this.out = out;
        this.format = format;
        this.format.validate();
        if (this.format.getHeader() != null && this.format.getHeader().length > 0) {
            printRecord((Object[]) this.format.getHeader());
        }
        // TODO: Is it a good idea to do this here instead of on the first call to a print method?
        // It seems a pain to have to track whether the header has already been printed or not.
    }