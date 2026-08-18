public CSVPrinter(final Appendable out, final CSVFormat format) throws IOException {
    Assertions.notNull(out, "out");
    Assertions.notNull(format, "format");

    this.out = out;
    this.format = format;
    this.format.validate();
    
    if (format.getHeader() != null) {
        this.printRecord((Object[]) format.getHeader());
    }
}