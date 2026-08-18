public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Assertions.notNull(url, "url");
        Assertions.notNull(charset, "charset");
        Assertions.notNull(format, "format");

        return new CSVParser(new InputStreamReader(new org.apache.commons.io.input.BOMInputStream(url.openStream()), charset), format);
    }