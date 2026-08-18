public CSVPrinter(final Appendable out, final CSVFormat format) throws IOException {
        // Accept null Appendable and Format per tests: use sensible defaults.
        this.out = (out != null) ? out : new StringBuilder();
        this.format = (format != null) ? format : CSVFormat.DEFAULT;
        // Do not validate here; validation (if any) should occur when actually printing.
    }