public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Assertions.notNull(url, "url");
        Assertions.notNull(charset, "charset");
        Assertions.notNull(format, "format");

        final InputStream inputStream = url.openStream();
        try {
            return new CSVParser(new InputStreamReader(inputStream, charset), format);
        } catch (final IOException | RuntimeException e) {
            try {
                inputStream.close();
            } catch (final IOException suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
    }