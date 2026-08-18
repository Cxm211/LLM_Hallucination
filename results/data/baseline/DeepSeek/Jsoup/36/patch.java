static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;
        if (charsetName == null) { 
            docData = Charset.forName(defaultCharset).decode(byteData).toString();
            doc = parser.parseInput(docData, baseUri);
            Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
            if (meta != null) {
                String foundCharset;
                if (meta.hasAttr("http-equiv")) {
                    foundCharset = getCharsetFromContentType(meta.attr("content"));
                } else {
                    foundCharset = meta.attr("charset");
                }
                if (foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)) {
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
        if (charsetName == null) charsetName = defaultCharset;
        if (doc == null) {
            if (docData.length() > 0 && docData.charAt(0) == 65279)
                docData = docData.substring(1);
            doc = parser.parseInput(docData, baseUri);
            doc.outputSettings().charset(charsetName);
        } else {
            doc.outputSettings().charset(charsetName);
        }
        return doc;
    }