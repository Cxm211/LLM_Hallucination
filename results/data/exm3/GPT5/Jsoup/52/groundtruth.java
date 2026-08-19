// ===== FIXED org.jsoup.helper.DataUtil :: parseByteData(ByteBuffer, String, String, Parser) [lines 94-140] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-52-fixed/src/main/java/org/jsoup/helper/DataUtil.java =====
    static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;

        // look for BOM - overrides any other header or input
        charsetName = detectCharsetFromBom(byteData, charsetName);

        if (charsetName == null) { // determine from meta. safe first parse as UTF-8
            // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
            docData = Charset.forName(defaultCharset).decode(byteData).toString();
            doc = parser.parseInput(docData, baseUri);
            Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
            String foundCharset = null; // if not found, will keep utf-8 as best attempt
            if (meta != null) {
                if (meta.hasAttr("http-equiv")) {
                    foundCharset = getCharsetFromContentType(meta.attr("content"));
                }
                if (foundCharset == null && meta.hasAttr("charset")) {
                    foundCharset = meta.attr("charset");
                }
            }
            // look for <?xml encoding='ISO-8859-1'?>
            if (foundCharset == null && doc.childNode(0) instanceof XmlDeclaration) {
                XmlDeclaration prolog = (XmlDeclaration) doc.childNode(0);
                if (prolog.name().equals("xml")) {
                    foundCharset = prolog.attr("encoding");
                }
            }
            foundCharset = validateCharset(foundCharset);

            if (foundCharset != null && !foundCharset.equals(defaultCharset)) { // need to re-decode
                foundCharset = foundCharset.trim().replaceAll("[\"']", "");
                charsetName = foundCharset;
                byteData.rewind();
                docData = Charset.forName(foundCharset).decode(byteData).toString();
                doc = null;
            }
        } else { // specified by content type header (or by user on file load)
            Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
            docData = Charset.forName(charsetName).decode(byteData).toString();
        }
        if (doc == null) {
            doc = parser.parseInput(docData, baseUri);
            doc.outputSettings().charset(charsetName);
        }
        return doc;
    }

// ===== FIXED org.jsoup.nodes.XmlDeclaration :: getWholeDeclaration() [lines 45-47] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-52-fixed/src/main/java/org/jsoup/nodes/XmlDeclaration.java =====
    public String getWholeDeclaration() {
        return attributes.html().trim(); // attr html starts with a " "
    }

// ===== FIXED org.jsoup.nodes.XmlDeclaration :: outerHtmlHead(Appendable, int, Document) [lines 49-58] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-52-fixed/src/main/java/org/jsoup/nodes/XmlDeclaration.java =====
	void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        accum
            .append("<")
            .append(isProcessingInstruction ? "!" : "?")
            .append(name);
        attributes.html(accum, out);
        accum
            .append(isProcessingInstruction ? "!" : "?")
            .append(">");
    }

// ===== FIXED org.jsoup.parser.XmlTreeBuilder :: insert(Token) [lines 55-68] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-52-fixed/src/main/java/org/jsoup/parser/XmlTreeBuilder.java =====
    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name());
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        Element el = new Element(tag, baseUri, startTag.attributes);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            tokeniser.acknowledgeSelfClosingFlag();
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }
