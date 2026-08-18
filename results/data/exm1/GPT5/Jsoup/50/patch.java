static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;

        // look for BOM - overrides any other header or input
        if (charsetName == null) { // determine from meta or BOM
            // detect BOM from raw bytes first
            int pos = byteData.position();
            int remaining = byteData.remaining();
            String bomCharset = null;
            int bomOffset = 0;
            if (remaining >= 4) {
                int b0 = byteData.get(pos) & 0xFF;
                int b1 = byteData.get(pos + 1) & 0xFF;
                int b2 = byteData.get(pos + 2) & 0xFF;
                int b3 = byteData.get(pos + 3) & 0xFF;
                if (b0 == 0x00 && b1 == 0x00 && b2 == 0xFE && b3 == 0xFF) { // UTF-32BE
                    bomCharset = "UTF-32BE";
                    bomOffset = 4;
                } else if (b0 == 0xFF && b1 == 0xFE && b2 == 0x00 && b3 == 0x00) { // UTF-32LE
                    bomCharset = "UTF-32LE";
                    bomOffset = 4;
                }
            }
            if (bomCharset == null && remaining >= 3) {
                int b0 = byteData.get(pos) & 0xFF;
                int b1 = byteData.get(pos + 1) & 0xFF;
                int b2 = byteData.get(pos + 2) & 0xFF;
                if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) { // UTF-8
                    bomCharset = "UTF-8";
                    bomOffset = 3;
                }
            }
            if (bomCharset == null && remaining >= 2) {
                int b0 = byteData.get(pos) & 0xFF;
                int b1 = byteData.get(pos + 1) & 0xFF;
                if (b0 == 0xFE && b1 == 0xFF) { // UTF-16BE
                    bomCharset = "UTF-16BE";
                    bomOffset = 2;
                } else if (b0 == 0xFF && b1 == 0xFE) { // UTF-16LE
                    bomCharset = "UTF-16LE";
                    bomOffset = 2;
                }
            }

            if (bomCharset != null) {
                charsetName = bomCharset;
                byteData.position(pos + bomOffset);
                docData = Charset.forName(bomCharset).decode(byteData).toString();
                doc = null; // will parse below and set output charset
            } else {
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