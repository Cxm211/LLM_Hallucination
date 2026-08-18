// buggy function
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
                    try {
                        if (Charset.isSupported(meta.attr("charset"))) {
                    foundCharset = meta.attr("charset");
                        }
                    } catch (IllegalCharsetNameException e) {
                        foundCharset = null;
                    }
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

    public String getWholeDeclaration() {
        final String decl = this.name;
        if(decl.equals("xml") && attributes.size() > 1 ) {
            StringBuilder sb = new StringBuilder(decl);
            final String version = attributes.get("version");
            if( version != null ) {
                sb.append(" version=\"").append(version).append("\"");
            }
            final String encoding = attributes.get("encoding");
            if( encoding != null ) {
                sb.append(" encoding=\"").append(encoding).append("\"");
            }
            return sb.toString();
        }
        else {
            return this.name;
        }
    }

	void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        accum
            .append("<")
            .append(isProcessingInstruction ? "!" : "?")
                .append(getWholeDeclaration())
            .append(">");
    }

    void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData(), baseUri);
        Node insert = comment;
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            String data = comment.getData();
            if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                String declaration = data.substring(1);
                insert = new XmlDeclaration(declaration, comment.baseUri(), data.startsWith("!"));
            }
        }
        insertNode(insert);
    }

// trigger testcase
// org/jsoup/nodes/DocumentTest.java::testMetaCharsetUpdateXmlDisabledNoChanges
@Test
    public void testMetaCharsetUpdateXmlDisabledNoChanges() {
        final Document doc = createXmlDocument("dontTouch", "dontTouch", true);
        
        final String xmlCharset = "<?xml version=\"dontTouch\" encoding=\"dontTouch\"?>\n" +
                                    "<root>\n" +
                                    " node\n" +
                                    "</root>";
        assertEquals(xmlCharset, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals("dontTouch", selectedNode.attr("encoding"));
        assertEquals("dontTouch", selectedNode.attr("version"));
    }

// org/jsoup/nodes/DocumentTest.java::testMetaCharsetUpdateXmlIso8859
@Test
    public void testMetaCharsetUpdateXmlIso8859() {
        final Document doc = createXmlDocument("1.0", "changeThis", true);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetIso8859));
        
        final String xmlCharsetISO = "<?xml version=\"1.0\" encoding=\"" + charsetIso8859 + "\"?>\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetISO, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetIso8859, doc.charset().name());
        assertEquals(charsetIso8859, selectedNode.attr("encoding"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org/jsoup/nodes/DocumentTest.java::testMetaCharsetUpdateXmlNoCharset
@Test
    public void testMetaCharsetUpdateXmlNoCharset() {
        final Document doc = createXmlDocument("1.0", "none", false);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String xmlCharsetUTF8 = "<?xml version=\"1.0\" encoding=\"" + charsetUtf8 + "\"?>\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetUTF8, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetUtf8, selectedNode.attr("encoding"));
    }

// org/jsoup/nodes/DocumentTest.java::testMetaCharsetUpdateXmlUtf8
@Test
    public void testMetaCharsetUpdateXmlUtf8() {
        final Document doc = createXmlDocument("1.0", "changeThis", true);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String xmlCharsetUTF8 = "<?xml version=\"1.0\" encoding=\"" + charsetUtf8 + "\"?>\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetUTF8, doc.toString());

        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetUtf8, doc.charset().name());
        assertEquals(charsetUtf8, selectedNode.attr("encoding"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org/jsoup/parser/XmlTreeBuilderTest.java::handlesXmlDeclarationAsDeclaration
@Test public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding=\"UTF-8\"?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

// org/jsoup/parser/XmlTreeBuilderTest.java::testDetectCharsetEncodingDeclaration
@Test
    public void testDetectCharsetEncodingDeclaration() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-charset.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://example.com/", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.charset().name());
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <data>äöåéü</data>",
            TextUtil.stripNewlines(doc.html()));
    }

// org/jsoup/parser/XmlTreeBuilderTest.java::testParseDeclarationAttributes
@Test
    public void testParseDeclarationAttributes() {
        String xml = "<?xml version='1' encoding='UTF-8' something='else'?><val>One</val>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("1", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("else", decl.attr("something"));
        assertEquals("version=\"1\" encoding=\"UTF-8\" something=\"else\"", decl.getWholeDeclaration());
        assertEquals("<?xml version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", decl.outerHtml());
    }
