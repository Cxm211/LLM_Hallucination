static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
    String docData;
    Document doc = null;

    // Detect BOM from raw bytes first
    if (byteData.remaining() >= 2) {
        byte[] bomBytes = new byte[4];
        byteData.mark();
        byteData.get(bomBytes, 0, Math.min(byteData.remaining(), 4));
        byteData.reset();
        if (bomBytes[0] == (byte)0xEF && bomBytes[1] == (byte)0xBB && bomBytes[2] == (byte)0xBF) {
            // UTF-8 BOM
            charsetName = "UTF-8";
            byteData.position(byteData.position() + 3); // skip BOM
        } else if (bomBytes[0] == (byte)0xFE && bomBytes[1] == (byte)0xFF) {
            // UTF-16BE BOM
            charsetName = "UTF-16BE";
            byteData.position(byteData.position() + 2);
        } else if (bomBytes[0] == (byte)0xFF && bomBytes[1] == (byte)0xFE) {
            // UTF-16LE BOM
            if (byteData.remaining() >= 4 && bomBytes[2] == 0x00 && bomBytes[3] == 0x00) {
                // UTF-32LE BOM
                charsetName = "UTF-32LE";
                byteData.position(byteData.position() + 4);
            } else {
                charsetName = "UTF-16LE";
                byteData.position(byteData.position() + 2);
            }
        } else if (bomBytes[0] == 0x00 && bomBytes[1] == 0x00 && bomBytes[2] == (byte)0xFE && bomBytes[3] == (byte)0xFF) {
            // UTF-32BE BOM
            charsetName = "UTF-32BE";
            byteData.position(byteData.position() + 4);
        }
    }

    if (charsetName == null) {
        docData = Charset.forName(defaultCharset).decode(byteData).toString();
        doc = parser.parseInput(docData, baseUri);
        Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
        if (meta != null) {
            String foundCharset = null;
            if (meta.hasAttr("http-equiv")) {
                foundCharset = getCharsetFromContentType(meta.attr("content"));
            }
            if (foundCharset == null && meta.hasAttr("charset")) {
                try {
                    if (Charset.isSupported(meta.attr("charset"))) {
                        foundCharset = meta.attr("charset");
                    }
                } catch (IllegalCharsetNameException e) {
                    foundCharset = null;
                }
            }
            if (foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)) {
                foundCharset = foundCharset.trim().replaceAll("[\"']", "");
                charsetName = foundCharset;
                byteData.rewind(); // rewind after BOM handling is already done, but BOM bytes are skipped.
                docData = Charset.forName(foundCharset).decode(byteData).toString();
                doc = null;
            }
        }
    } else {
        Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
        docData = Charset.forName(charsetName).decode(byteData).toString();
    }

    if (doc == null) {
        doc = parser.parseInput(docData, baseUri);
        doc.outputSettings().charset(charsetName);
    }
    return doc;
}