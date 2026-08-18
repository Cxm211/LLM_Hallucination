static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;

        // detect BOM first - overrides any other header or input
        if (charsetName == null) {
            String bomCharset = null;
            int pos = byteData.position();
            if (byteData.remaining() >= 4) {
                byteData.mark();
                byte b0 = byteData.get();
                byte b1 = byteData.get();
                byte b2 = byteData.get();
                byte b3 = byteData.get();
                byteData.reset();
                if ((b0 == (byte) 0x00) && (b1 == (byte) 0x00) && (b2 == (byte) 0xFE) && (b3 == (byte) 0xFF)) {
                    bomCharset = "UTF-32BE";
                } else if ((b0 == (byte) 0xFF) && (b1 == (byte) 0xFE) && (b2 == (byte) 0x00) && (b3 == (byte) 0x00)) {
                    bomCharset = "UTF-32LE";
                } else if ((b0 == (byte) 0xEF) && (b1 == (byte) 0xBB) && (b2 == (byte) 0xBF)) {
                    bomCharset = "UTF-8";
                } else if ((b0 == (byte) 0xFE) && (b1 == (byte) 0xFF)) {
                    bomCharset = "UTF-16BE";
                } else if ((b0 == (byte) 0xFF) && (b1 == (byte) 0xFE)) {
                    bomCharset = "UTF-16LE";
                }
            }
            // restore original position if changed by detection
            byteData.position(pos);

            if (bomCharset != null) {
                charsetName = bomCharset;
                docData = Charset.forName(charsetName).decode(byteData).toString();
                // strip BOM char if present after decoding
                if (docData.length() > 0 && docData.charAt(0) == UNICODE_BOM) {
                    docData = docData.substring(1);
                }
            } else {
                // determine from meta. safe parse as UTF-8
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
            }
        } else { // specified by content type header (or by user on file load)
            Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
            docData = Charset.forName(charsetName).decode(byteData).toString();
        }
        // If decoded data starts with BOM char, just strip it (do not force default charset)
        if (docData.length() > 0 && docData.charAt(0) == UNICODE_BOM) {
            docData = docData.substring(1);
        }
        if (doc == null) {
            doc = parser.parseInput(docData, baseUri);
            doc.outputSettings().charset(charsetName);
        }
        return doc;
    }