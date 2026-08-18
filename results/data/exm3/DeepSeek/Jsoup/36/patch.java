    static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;
        if (charsetName == null) { // determine from meta. safe parse as UTF-8
            // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
            docData = Charset.forName(defaultCharset).decode(byteData).toString();
            doc = parser.parseInput(docData, baseUri);
            Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
            if (meta != null) { // if not found, will keep utf-8 as best attempt

                String foundCharset;
                if (meta.hasAttr("http-equiv")) {
                    foundCharset = getCharsetFromContentType(meta.attr("content"));
                } else {
                    foundCharset = meta.attr("charset");
                }

                // validate foundCharset
                String validatedCharset = null;
                if (foundCharset != null && !foundCharset.isEmpty()) {
                    String cs = foundCharset.trim();
                    if (Charset.isSupported(cs)) {
                        validatedCharset = cs;
                    } else {
                        cs = cs.toUpperCase(Locale.ENGLISH);
                        if (Charset.isSupported(cs)) {
                            validatedCharset = cs;
                        }
                    }
                }

                if (validatedCharset != null && !validatedCharset.equals(defaultCharset)) { // need to re-decode
                    charsetName = validatedCharset;
                    byteData.rewind();
                    docData = Charset.forName(validatedCharset).decode(byteData).toString();
                    doc = null;
                }
            }
        } else { // specified by content type header (or by user on file load)
            Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
            docData = Charset.forName(charsetName).decode(byteData).toString();
        }
        if (doc == null) {
            // there are times where there is a spurious byte-order-mark at the start of the text. Shouldn't be present
            // in utf-8. If after decoding, there is a BOM, strip it; otherwise will cause the parser to go straight
            // into head mode
            if (docData.length() > 0 && docData.charAt(0) == 65279)
                docData = docData.substring(1);

            doc = parser.parseInput(docData, baseUri);
        }
        // set output charset
        String outputCharset = charsetName != null ? charsetName : defaultCharset;
        doc.outputSettings().charset(outputCharset);
        return doc;
    }