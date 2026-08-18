static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
    String docData;
    Document doc = null;

    // look for BOM - overrides any other header or input
    byteData.mark();
    byte[] bom4 = new byte[4];
    if (byteData.remaining() >= 4) {
        byteData.get(bom4);
        byteData.rewind();
    }
    BomCharset bomCharset = detectCharsetFromBom(bom4);
    if (bomCharset.charset != null) {
        charsetName = bomCharset.charset;
        if (bomCharset.offset > 0) {
            byteData.position(bomCharset.offset);
        }
    }

    if (charsetName == null) { // determine from meta. safe parse as UTF-8
        // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
        docData = Charset.forName(defaultCharset).decode(byteData).toString();
        doc = parser.parseInput(docData, baseUri);
        Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
        if (meta != null) { // if not found, will keep utf-8 as best attempt
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

            if (foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)) { // need to re-decode
                foundCharset = foundCharset.trim().replaceAll("[\"']", "");
                charsetName = foundCharset;
                byteData.rewind();
                docData = Charset.forName(foundCharset).decode(byteData).toString();
                doc = null;
            }
        }
    } else { // specified by content type header (or by user on file load)
        Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
        docData = Charset.forName(charsetName).decode(byteData).toString();
    }
    if (docData.length() > 0 && docData.charAt(0) == UNICODE_BOM) {
        docData = docData.substring(1);
    }
    if (doc == null) {
        doc = parser.parseInput(docData, baseUri);
        doc.outputSettings().charset(charsetName);
    }
    return doc;
}

private static class BomCharset {
    final String charset;
    final int offset;
    BomCharset(String charset, int offset) {
        this.charset = charset;
        this.offset = offset;
    }
}

private static BomCharset detectCharsetFromBom(byte[] bom) {
    if (bom.length >= 4) {
        if (bom[0] == (byte)0x00 && bom[1] == (byte)0x00 && bom[2] == (byte)0xFE && bom[3] == (byte)0xFF) {
            return new BomCharset("UTF-32BE", 4);
        }
        if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE && bom[2] == (byte)0x00 && bom[3] == (byte)0x00) {
            return new BomCharset("UTF-32LE", 4);
        }
    }
    if (bom.length >= 3) {
        if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
            return new BomCharset("UTF-8", 3);
        }
    }
    if (bom.length >= 2) {
        if (bom[0] == (byte)0xFE && bom[1] == (byte)0xFF) {
            return new BomCharset("UTF-16BE", 2);
        }
        if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE) {
            return new BomCharset("UTF-16LE", 2);
        }
    }
    return new BomCharset(null, 0);
}