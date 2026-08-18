static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
    // Detect and consume BOM
    String bomCharset = null;
    int bomLength = 0;
    byteData.mark();
    byteData.rewind();
    if (byteData.remaining() >= 3) {
        byte b1 = byteData.get();
        byte b2 = byteData.get();
        byte b3 = byteData.get();
        if (b1 == (byte)0xEF && b2 == (byte)0xBB && b3 == (byte)0xBF) {
            bomCharset = "UTF-8";
            bomLength = 3;
        } else {
            byteData.rewind();
        }
    }
    if (bomCharset == null && byteData.remaining() >= 2) {
        byte b1 = byteData.get();
        byte b2 = byteData.get();
        if (b1 == (byte)0xFE && b2 == (byte)0xFF) {
            bomCharset = "UTF-16BE";
            bomLength = 2;
        } else if (b1 == (byte)0xFF && b2 == (byte)0xFE) {
            bomCharset = "UTF-16LE";
            bomLength = 2;
        } else {
            byteData.rewind();
        }
    }
    if (bomCharset == null && byteData.remaining() >= 4) {
        byte b1 = byteData.get();
        byte b2 = byteData.get();
        byte b3 = byteData.get();
        byte b4 = byteData.get();
        if (b1 == 0x00 && b2 == 0x00 && b3 == (byte)0xFE && b4 == (byte)0xFF) {
            bomCharset = "UTF-32BE";
            bomLength = 4;
        } else if (b1 == (byte)0xFF && b2 == (byte)0xFE && b3 == 0x00 && b4 == 0x00) {
            bomCharset = "UTF-32LE";
            bomLength = 4;
        } else {
            byteData.rewind();
        }
    }
    if (bomCharset != null) {
        charsetName = bomCharset;
        byteData.rewind();
        byteData.position(bomLength);
    } else {
        byteData.reset();
    }

    String docData;
    Document doc = null;

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
                byteData.rewind();
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