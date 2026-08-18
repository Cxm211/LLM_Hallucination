static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
    String docData;
    Document doc = null;
    
    // BOM detection
    boolean bomDetected = false;
    byteData.mark();
    if (byteData.remaining() >= 4) {
        byte[] bom = new byte[4];
        byteData.get(bom);
        if (bom[0] == (byte)0x00 && bom[1] == (byte)0x00 && bom[2] == (byte)0xFE && bom[3] == (byte)0xFF) {
            charsetName = "UTF-32BE";
            bomDetected = true;
        } else if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE && bom[2] == (byte)0x00 && bom[3] == (byte)0x00) {
            charsetName = "UTF-32LE";
            bomDetected = true;
        } else if (bom[0] == (byte)0xFE && bom[1] == (byte)0xFF) {
            charsetName = "UTF-16BE";
            bomDetected = true;
            byteData.position(byteData.position() - 2);
        } else if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE) {
            charsetName = "UTF-16LE";
            bomDetected = true;
            byteData.position(byteData.position() - 2);
        } else if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
            charsetName = "UTF-8";
            bomDetected = true;
            byteData.position(byteData.position() - 1);
        } else {
            byteData.reset();
        }
    } else {
        byteData.reset();
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
                byteData.rewind();
                docData = Charset.forName(foundCharset).decode(byteData).toString();
                doc = null;
            }
        }
    } else {
        Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
        docData = Charset.forName(charsetName).decode(byteData).toString();
    }
    if (!bomDetected && docData.length() > 0 && docData.charAt(0) == UNICODE_BOM) {
        byteData.rewind();
        docData = Charset.forName(defaultCharset).decode(byteData).toString();
        docData = docData.substring(1);
        charsetName = defaultCharset;
        doc = null;
    }
    if (doc == null) {
        doc = parser.parseInput(docData, baseUri);
        doc.outputSettings().charset(charsetName);
    }
    return doc;
}