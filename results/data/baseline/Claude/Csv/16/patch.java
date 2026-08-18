public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Assertions.notNull(url, "url");
        Assertions.notNull(charset, "charset");
        Assertions.notNull(format, "format");

        final InputStreamReader reader = new InputStreamReader(url.openStream(), charset);
        try {
            return new CSVParser(reader, format, 0L, 1L);
        } catch (final IOException | RuntimeException e) {
            try {
                reader.close();
            } catch (final IOException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
    }