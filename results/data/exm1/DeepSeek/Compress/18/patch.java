void writePaxHeaders(String entryName,
                         Map<String, String> headers) throws IOException {
    String name = "./PaxHeaders.X/" + stripTo7Bits(entryName);
    if (name.length() >= TarConstants.NAMELEN) {
        name = name.substring(0, TarConstants.NAMELEN - 1);
    }
    TarArchiveEntry pex = new TarArchiveEntry(name,
                                              TarConstants.LF_PAX_EXTENDED_HEADER_LC);

    StringWriter w = new StringWriter();
    for (Map.Entry<String, String> h : headers.entrySet()) {
        String key = h.getKey();
        String value = h.getValue();
        int len = key.length() + value.length()
            + 3 /* blank, equals and newline */
            + 2 /* guess 9 < actual length < 100 */;
        String line;
        do {
            line = len + " " + key + "=" + value + "\n";
            int actualLength = line.getBytes(CharsetNames.UTF_8).length;
            if (len == actualLength) break;
            len = actualLength;
        } while (true);
        w.write(line);
    }
    byte[] data = w.toString().getBytes(CharsetNames.UTF_8);
    pex.setSize(data.length);
    putArchiveEntry(pex);
    write(data);
    closeArchiveEntry();
}