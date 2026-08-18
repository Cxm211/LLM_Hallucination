static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
    String docData;
    Document doc = null;

    // look for BOM - overrides any other header or input
    byteData.mark();
    if (byteData.remaining() >= 2) {
        int byte1 = byteData.get() & 0xFF;
        int byte2 = byteData.get() & 0xFF;
        if ((byte1 == 0xFE && byte2 == 0xFF) || (byte1 == 0xFF && byte2 == 0xFE)) {
            byteData.reset();
            if (byte1 == 0xFE && byte2 == 0xFF) {
                charsetName = "UTF-16BE";
            } else {
                charsetName = "UTF-16LE";
            }
            docData = Charset.forName(charsetName).decode(byteData).toString();
            doc = parser.parseInput(docData, baseUri);
            doc.outputSettings().charset(charsetName);
            return doc;
        } else if (byteData.remaining() >= 2) {
            int byte3 = byteData.get() & 0xFF;
            int byte4 = byteData.get() & 0xFF;
            if ((byte1 == 0x00 && byte2 == 0x00 && byte3 == 0xFE && byte4 == 0xFF) ||
                (byte1 == 0xFF && byte2 == 0xFE && byte3 == 0x00 && byte4 == 0x00)) {
                byteData.reset();
                if (byte1 == 0x00 && byte2 == 0x00) {
                    charsetName = "UTF-32BE";
                } else {
                    charsetName = "UTF-32LE";
                }
                docData = Charset.forName(charsetName).decode(byteData).toString();
                doc = parser.parseInput(docData, baseUri);
                doc.outputSettings().charset(charsetName);
                return doc;
            }
        }
    }
    byteData.reset();

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