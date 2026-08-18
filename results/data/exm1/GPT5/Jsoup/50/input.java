// buggy code
    static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser) {
        String docData;
        Document doc = null;

        // look for BOM - overrides any other header or input

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

// relevant test
// org.jsoup.helper.DataUtilTest::testCharset
    public void testCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8 "));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html"));
        assertEquals(null, DataUtil.getCharsetFromContentType(null));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html;charset=Unknown"));
    }

// org.jsoup.helper.DataUtilTest::testQuotedCharset
    @Test public void testQuotedCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=\"UTF-8\""));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=\"Unsupported\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
    }

// org.jsoup.helper.DataUtilTest::discardsSpuriousByteOrderMark
    @Test public void discardsSpuriousByteOrderMark() {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://foo.com/", Parser.htmlParser());
        assertEquals("One", doc.head().text());
    }

// org.jsoup.helper.DataUtilTest::discardsSpuriousByteOrderMarkWhenNoCharsetSet
    @Test public void discardsSpuriousByteOrderMarkWhenNoCharsetSet() {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
        Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com/", Parser.htmlParser());
        assertEquals("One", doc.head().text());
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
    }

// org.jsoup.helper.DataUtilTest::shouldNotThrowExceptionOnEmptyCharset
    public void shouldNotThrowExceptionOnEmptyCharset() {
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset="));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=;"));
    }

// org.jsoup.helper.DataUtilTest::shouldSelectFirstCharsetOnWeirdMultileCharsetsInMetaTags
    public void shouldSelectFirstCharsetOnWeirdMultileCharsetsInMetaTags() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1, charset=1251"));
    }

// org.jsoup.helper.DataUtilTest::shouldCorrectCharsetForDuplicateCharsetString
    public void shouldCorrectCharsetForDuplicateCharsetString() {
        assertEquals("iso-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=charset=iso-8859-1"));
    }

// org.jsoup.helper.DataUtilTest::shouldReturnNullForIllegalCharsetNames
    public void shouldReturnNullForIllegalCharsetNames() {
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=$HJKDF§$/("));
    }

// org.jsoup.helper.DataUtilTest::generatesMimeBoundaries
    public void generatesMimeBoundaries() {
        String m1 = DataUtil.mimeBoundary();
        String m2 = DataUtil.mimeBoundary();

        assertEquals(DataUtil.boundaryLength, m1.length());
        assertEquals(DataUtil.boundaryLength, m2.length());
        assertNotSame(m1, m2);
    }

// org.jsoup.helper.DataUtilTest::wrongMetaCharsetFallback
    public void wrongMetaCharsetFallback() {
        try {
            final byte[] input = "<html><head><meta charset=iso-8></head><body></body></html>".getBytes("UTF-8");
            final ByteBuffer inBuffer = ByteBuffer.wrap(input);
            
            Document doc = DataUtil.parseByteData(inBuffer, null, "http://example.com", Parser.htmlParser());
            
            final String expected = "<html>\n" +
                                    " <head>\n" +
                                    "  <meta charset=\"iso-8\">\n" +
                                    " </head>\n" +
                                    " <body></body>\n" +
                                    "</html>";
            
            assertEquals(expected, doc.toString());
        } catch( UnsupportedEncodingException ex ) {
            fail(ex.getMessage());
        }
    }

// org.jsoup.helper.DataUtilTest::supportsBOMinFiles
    public void supportsBOMinFiles() throws IOException {
        
        File in = getFile("/bomtests/bom_utf16be.html");
        Document doc = Jsoup.parse(in, null, "http://example.com");
        assertTrue(doc.title().contains("UTF-16BE"));
        assertTrue(doc.text().contains("가각갂갃간갅"));

        in = getFile("/bomtests/bom_utf16le.html");
        doc = Jsoup.parse(in, null, "http://example.com");
        assertTrue(doc.title().contains("UTF-16LE"));
        assertTrue(doc.text().contains("가각갂갃간갅"));

        in = getFile("/bomtests/bom_utf32be.html");
        doc = Jsoup.parse(in, null, "http://example.com");
        assertTrue(doc.title().contains("UTF-32BE"));
        assertTrue(doc.text().contains("가각갂갃간갅"));

        in = getFile("/bomtests/bom_utf32le.html");
        doc = Jsoup.parse(in, null, "http://example.com");
        assertTrue(doc.title().contains("UTF-32LE"));
        assertTrue(doc.text().contains("가각갂갃간갅"));
    }

// org.jsoup.helper.W3CDomTest::simpleConversion
    public void simpleConversion() {}

// org.jsoup.helper.W3CDomTest::convertsGoogle
    public void convertsGoogle() throws IOException {
        File in = ParseTest.getFile("/htmltests/google-ipod.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(in, "UTF8");

        W3CDom w3c = new W3CDom();
        Document wDoc = w3c.fromJsoup(doc);
        Node htmlEl = wDoc.getChildNodes().item(0);
        assertEquals(null, htmlEl.getNamespaceURI());
        assertEquals("html", htmlEl.getLocalName());
        assertEquals("html", htmlEl.getNodeName());

        String out = w3c.asString(wDoc);
        assertTrue(out.contains("ipod"));
    }

// org.jsoup.helper.W3CDomTest::namespacePreservation
    public void namespacePreservation() throws IOException {
        File in = ParseTest.getFile("/htmltests/namespaces.xhtml");
        org.jsoup.nodes.Document jsoupDoc;
        jsoupDoc = Jsoup.parse(in, "UTF-8");

        Document doc;
        org.jsoup.helper.W3CDom jDom = new org.jsoup.helper.W3CDom();
        doc = jDom.fromJsoup(jsoupDoc);

        Node htmlEl = doc.getChildNodes().item(0);
        assertEquals("http://www.w3.org/1999/xhtml", htmlEl.getNamespaceURI());
        assertEquals("html", htmlEl.getLocalName());
        assertEquals("html", htmlEl.getNodeName());

        Node epubTitle = htmlEl.getChildNodes().item(2).getChildNodes().item(3);
        assertEquals("http://www.idpf.org/2007/ops", epubTitle.getNamespaceURI());
        assertEquals("title", epubTitle.getLocalName());
        assertEquals("epub:title", epubTitle.getNodeName());

        Node xSection = epubTitle.getNextSibling().getNextSibling();
        assertEquals("urn:test", xSection.getNamespaceURI());
        assertEquals("section", xSection.getLocalName());
        assertEquals("x:section", xSection.getNodeName());
    }

// org.jsoup.integration.ParseTest::testSmhBizArticle
    public void testSmhBizArticle() throws IOException {
        File in = getFile("/htmltests/smh-biz-article-1.html");
        Document doc = Jsoup.parse(in, "UTF-8",
                "http://www.smh.com.au/business/the-boards-next-fear-the-female-quota-20100106-lteq.html");
        assertEquals("The board’s next fear: the female quota",
                doc.title()); 
        assertEquals("en", doc.select("html").attr("xml:lang"));

        Elements articleBody = doc.select(".articleBody > *");
        assertEquals(17, articleBody.size());
        

    }

// org.jsoup.integration.ParseTest::testNewsHomepage
    public void testNewsHomepage() throws IOException {
        File in = getFile("/htmltests/news-com-au-home.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.news.com.au/");
        assertEquals("News.com.au | News from Australia and around the world online | NewsComAu", doc.title());
        assertEquals("Brace yourself for Metro meltdown", doc.select(".id1225817868581 h4").text().trim());

        Element a = doc.select("a[href=/entertainment/horoscopes]").first();
        assertEquals("/entertainment/horoscopes", a.attr("href"));
        assertEquals("http://www.news.com.au/entertainment/horoscopes", a.attr("abs:href"));

        Element hs = doc.select("a[href*=naughty-corners-are-a-bad-idea]").first();
        assertEquals(
                "http://www.heraldsun.com.au/news/naughty-corners-are-a-bad-idea-for-kids/story-e6frf7jo-1225817899003",
                hs.attr("href"));
        assertEquals(hs.attr("href"), hs.attr("abs:href"));
    }

// org.jsoup.integration.ParseTest::testGoogleSearchIpod
    public void testGoogleSearchIpod() throws IOException {
        File in = getFile("/htmltests/google-ipod.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.google.com/search?hl=en&q=ipod&aq=f&oq=&aqi=g10");
        assertEquals("ipod - Google Search", doc.title());
        Elements results = doc.select("h3.r > a");
        assertEquals(12, results.size());
        assertEquals(
                "http://news.google.com/news?hl=en&q=ipod&um=1&ie=UTF-8&ei=uYlKS4SbBoGg6gPf-5XXCw&sa=X&oi=news_group&ct=title&resnum=1&ved=0CCIQsQQwAA",
                results.get(0).attr("href"));
        assertEquals("http://www.apple.com/itunes/",
                results.get(1).attr("href"));
    }

// org.jsoup.integration.ParseTest::testBinary
    public void testBinary() throws IOException {
        File in = getFile("/htmltests/thumb.jpg");
        Document doc = Jsoup.parse(in, "UTF-8");
        
        assertTrue(doc.text().contains("gd-jpeg"));
    }

// org.jsoup.integration.ParseTest::testYahooJp
    public void testYahooJp() throws IOException {
        File in = getFile("/htmltests/yahoo-jp.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.yahoo.co.jp/index.html"); 
        assertEquals("Yahoo! JAPAN", doc.title());
        Element a = doc.select("a[href=t/2322m2]").first();
        assertEquals("http://www.yahoo.co.jp/_ylh=X3oDMTB0NWxnaGxsBF9TAzIwNzcyOTYyNjUEdGlkAzEyBHRtcGwDZ2Ex/t/2322m2",
                a.attr("abs:href")); 
        assertEquals("全国、人気の駅ランキング", a.text());
    }

// org.jsoup.integration.ParseTest::testBaidu
    public void testBaidu() throws IOException {
        
        File in = getFile("/htmltests/baidu-cn-home.html");
        Document doc = Jsoup.parse(in, null,
                "http://www.baidu.com/"); 
        Element submit = doc.select("#su").first();
        assertEquals("百度一下", submit.attr("value"));

        
        submit = doc.select("input[value=百度一下]").first();
        assertEquals("su", submit.id());
        Element newsLink = doc.select("a:contains(新)").first();
        assertEquals("http://news.baidu.com", newsLink.absUrl("href"));

        
        assertEquals("GB2312", doc.outputSettings().charset().displayName());
        assertEquals("<title>百度一下，你就知道      </title>", doc.select("title").outerHtml());

        doc.outputSettings().charset("ascii");
        assertEquals("<title>&#x767e;&#x5ea6;&#x4e00;&#x4e0b;&#xff0c;&#x4f60;&#x5c31;&#x77e5;&#x9053;      </title>",
                doc.select("title").outerHtml());
    }

// org.jsoup.integration.ParseTest::testBaiduVariant
    public void testBaiduVariant() throws IOException {
        
        File in = getFile("/htmltests/baidu-variant.html");
        Document doc = Jsoup.parse(in, null,
                "http://www.baidu.com/"); 
        
        assertEquals("GB2312", doc.outputSettings().charset().displayName());
        assertEquals("<title>百度一下，你就知道</title>", doc.select("title").outerHtml());
    }

// org.jsoup.integration.ParseTest::testHtml5Charset
    public void testHtml5Charset() throws IOException {
        
        File in = getFile("/htmltests/meta-charset-1.html");
        Document doc = Jsoup.parse(in, null, "http://example.com/"); 
        assertEquals("新", doc.text());
        assertEquals("GB2312", doc.outputSettings().charset().displayName());

        
        in = getFile("/htmltests/meta-charset-2.html"); 
        doc = Jsoup.parse(in, null, "http://example.com"); 
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
        assertFalse("新".equals(doc.text()));

        
        in = getFile("/htmltests/meta-charset-3.html");
        doc = Jsoup.parse(in, null, "http://example.com/"); 
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
        assertEquals("新", doc.text());
    }

// org.jsoup.integration.ParseTest::testBrokenHtml5CharsetWithASingleDoubleQuote
    public void testBrokenHtml5CharsetWithASingleDoubleQuote() throws IOException {
        InputStream in = inputStreamFrom("<html>\n" +
                "<head><meta charset=UTF-8\"></head>\n" +
                "<body></body>\n" +
                "</html>");
        Document doc = Jsoup.parse(in, null, "http://example.com/");
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
    }

// org.jsoup.integration.ParseTest::testNytArticle
    public void testNytArticle() throws IOException {
        
        File in = getFile("/htmltests/nyt-article-1.html");
        Document doc = Jsoup.parse(in, null, "http://www.nytimes.com/2010/07/26/business/global/26bp.html?hp");

        Element headline = doc.select("nyt_headline[version=1.0]").first();
        assertEquals("As BP Lays Out Future, It Will Not Include Hayward", headline.text());
    }

// org.jsoup.integration.ParseTest::testYahooArticle
    public void testYahooArticle() throws IOException {
        File in = getFile("/htmltests/yahoo-article-1.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://news.yahoo.com/s/nm/20100831/bs_nm/us_gm_china");
        Element p = doc.select("p:contains(Volt will be sold in the United States").first();
        assertEquals("In July, GM said its electric Chevrolet Volt will be sold in the United States at $41,000 -- $8,000 more than its nearest competitor, the Nissan Leaf.", p.text());
    }

// org.jsoup.nodes.DocumentTest::setTextPreservesDocumentStructure
    @Test public void setTextPreservesDocumentStructure() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        doc.text("Replaced");
        assertEquals("Replaced", doc.text());
        assertEquals("Replaced", doc.body().text());
        assertEquals(1, doc.select("head").size());
    }

// org.jsoup.nodes.DocumentTest::testTitles
    @Test public void testTitles() {
        Document noTitle = Jsoup.parse("<p>Hello</p>");
        Document withTitle = Jsoup.parse("<title>First</title><title>Ignore</title><p>Hello</p>");
        
        assertEquals("", noTitle.title());
        noTitle.title("Hello");
        assertEquals("Hello", noTitle.title());
        assertEquals("Hello", noTitle.select("title").first().text());
        
        assertEquals("First", withTitle.title());
        withTitle.title("Hello");
        assertEquals("Hello", withTitle.title());
        assertEquals("Hello", withTitle.select("title").first().text());

        Document normaliseTitle = Jsoup.parse("<title>   Hello\nthere   \n   now   \n");
        assertEquals("Hello there now", normaliseTitle.title());
    }

// org.jsoup.nodes.DocumentTest::testOutputEncoding
    @Test public void testOutputEncoding() {
        Document doc = Jsoup.parse("<p title=π>π & < > </p>");
        
        assertEquals("<p title=\"π\">π &amp; &lt; &gt; </p>", doc.body().html());
        assertEquals("UTF-8", doc.outputSettings().charset().name());

        doc.outputSettings().charset("ascii");
        assertEquals(Entities.EscapeMode.base, doc.outputSettings().escapeMode());
        assertEquals("<p title=\"&#x3c0;\">&#x3c0; &amp; &lt; &gt; </p>", doc.body().html());

        doc.outputSettings().escapeMode(Entities.EscapeMode.extended);
        assertEquals("<p title=\"&pi;\">&pi; &amp; &lt; &gt; </p>", doc.body().html());
    }

// org.jsoup.nodes.DocumentTest::testXhtmlReferences
    @Test public void testXhtmlReferences() {
        Document doc = Jsoup.parse("&lt; &gt; &amp; &quot; &apos; &times;");
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("&lt; &gt; &amp; \" ' ×", doc.body().html());
    }

// org.jsoup.nodes.DocumentTest::testNormalisesStructure
    @Test public void testNormalisesStructure() {
        Document doc = Jsoup.parse("<html><head><script>one</script><noscript><p>two</p></noscript></head><body><p>three</p></body><p>four</p></html>");
        assertEquals("<html><head><script>one</script><noscript>&lt;p&gt;two</noscript></head><body><p>three</p><p>four</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.nodes.DocumentTest::testClone
    @Test public void testClone() {
        Document doc = Jsoup.parse("<title>Hello</title> <p>One<p>Two");
        Document clone = doc.clone();

        assertEquals("<html><head><title>Hello</title> </head><body><p>One</p><p>Two</p></body></html>", TextUtil.stripNewlines(clone.html()));
        clone.title("Hello there");
        clone.select("p").first().text("One more").attr("id", "1");
        assertEquals("<html><head><title>Hello there</title> </head><body><p id=\"1\">One more</p><p>Two</p></body></html>", TextUtil.stripNewlines(clone.html()));
        assertEquals("<html><head><title>Hello</title> </head><body><p>One</p><p>Two</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.nodes.DocumentTest::testClonesDeclarations
    @Test public void testClonesDeclarations() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>Doctype test");
        Document clone = doc.clone();

        assertEquals(doc.html(), clone.html());
        assertEquals("<!doctype html><html><head><title>Doctype test</title></head><body></body></html>",
                TextUtil.stripNewlines(clone.html()));
    }

// org.jsoup.nodes.DocumentTest::testLocation
    @Test public void testLocation() throws IOException {
    	File in = new ParseTest().getFile("/htmltests/yahoo-jp.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.yahoo.co.jp/index.html");
        String location = doc.location();
        String baseUri = doc.baseUri();
        assertEquals("http://www.yahoo.co.jp/index.html",location);
        assertEquals("http://www.yahoo.co.jp/_ylh=X3oDMTB0NWxnaGxsBF9TAzIwNzcyOTYyNjUEdGlkAzEyBHRtcGwDZ2Ex/",baseUri);
        in = new ParseTest().getFile("/htmltests/nyt-article-1.html");
        doc = Jsoup.parse(in, null, "http://www.nytimes.com/2010/07/26/business/global/26bp.html?hp");
        location = doc.location();
        baseUri = doc.baseUri();
        assertEquals("http://www.nytimes.com/2010/07/26/business/global/26bp.html?hp",location);
        assertEquals("http://www.nytimes.com/2010/07/26/business/global/26bp.html?hp",baseUri);
    }

// org.jsoup.nodes.DocumentTest::testHtmlAndXmlSyntax
    @Test public void testHtmlAndXmlSyntax() {
        String h = "<!DOCTYPE html><body><img async checked='checked' src='&<>\"'>&lt;&gt;&amp;&quot;<foo />bar";
        Document doc = Jsoup.parse(h);

        doc.outputSettings().syntax(Syntax.html);
        assertEquals("<!doctype html>\n" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img async checked src=\"&amp;<>&quot;\">&lt;&gt;&amp;\"\n" +
                "  <foo />bar\n" +
                " </body>\n" +
                "</html>", doc.html());

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        assertEquals("<!DOCTYPE html>\n" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img async=\"\" checked=\"checked\" src=\"&amp;<>&quot;\" />&lt;&gt;&amp;\"\n" +
                "  <foo />bar\n" +
                " </body>\n" +
                "</html>", doc.html());
    }

// org.jsoup.nodes.DocumentTest::htmlParseDefaultsToHtmlOutputSyntax
    @Test public void htmlParseDefaultsToHtmlOutputSyntax() {
        Document doc = Jsoup.parse("x");
        assertEquals(Syntax.html, doc.outputSettings().syntax());
    }

// org.jsoup.nodes.DocumentTest::testOverflowClone
    @Test public void testOverflowClone() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            builder.insert(0, "<i>");
            builder.append("</i>");
        }

        Document doc = Jsoup.parse(builder.toString());
        doc.clone();
    }

// org.jsoup.nodes.DocumentTest::DocumentsWithSameContentAreEqual
    @Test public void DocumentsWithSameContentAreEqual() throws Exception {
        Document docA = Jsoup.parse("<div/>One");
        Document docB = Jsoup.parse("<div/>One");
        Document docC = Jsoup.parse("<div/>Two");

        assertFalse(docA.equals(docB));
        assertTrue(docA.equals(docA));
        assertEquals(docA.hashCode(), docA.hashCode());
        assertFalse(docA.hashCode() == docC.hashCode());
    }

// org.jsoup.nodes.DocumentTest::DocumentsWithSameContentAreVerifialbe
    @Test public void DocumentsWithSameContentAreVerifialbe() throws Exception {
        Document docA = Jsoup.parse("<div/>One");
        Document docB = Jsoup.parse("<div/>One");
        Document docC = Jsoup.parse("<div/>Two");

        assertTrue(docA.hasSameValue(docB));
        assertFalse(docA.hasSameValue(docC));
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateUtf8
    public void testMetaCharsetUpdateUtf8() {
        final Document doc = createHtmlDocument("changeThis");
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String htmlCharsetUTF8 = "<html>\n" +
                                        " <head>\n" +
                                        "  <meta charset=\"" + charsetUtf8 + "\">\n" +
                                        " </head>\n" +
                                        " <body></body>\n" +
                                        "</html>";
        assertEquals(htmlCharsetUTF8, doc.toString());
        
        Element selectedElement = doc.select("meta[charset]").first();
        assertEquals(charsetUtf8, doc.charset().name());
        assertEquals(charsetUtf8, selectedElement.attr("charset"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateIso8859
    public void testMetaCharsetUpdateIso8859() {
        final Document doc = createHtmlDocument("changeThis");
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetIso8859));
        
        final String htmlCharsetISO = "<html>\n" +
                                        " <head>\n" +
                                        "  <meta charset=\"" + charsetIso8859 + "\">\n" +
                                        " </head>\n" +
                                        " <body></body>\n" +
                                        "</html>";
        assertEquals(htmlCharsetISO, doc.toString());
        
        Element selectedElement = doc.select("meta[charset]").first();
        assertEquals(charsetIso8859, doc.charset().name());
        assertEquals(charsetIso8859, selectedElement.attr("charset"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateNoCharset
    public void testMetaCharsetUpdateNoCharset() {
        final Document docNoCharset = Document.createShell("");
        docNoCharset.updateMetaCharsetElement(true);
        docNoCharset.charset(Charset.forName(charsetUtf8));
        
        assertEquals(charsetUtf8, docNoCharset.select("meta[charset]").first().attr("charset"));
        
        final String htmlCharsetUTF8 = "<html>\n" +
                                        " <head>\n" +
                                        "  <meta charset=\"" + charsetUtf8 + "\">\n" +
                                        " </head>\n" +
                                        " <body></body>\n" +
                                        "</html>";
        assertEquals(htmlCharsetUTF8, docNoCharset.toString()); 
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateDisabled
    public void testMetaCharsetUpdateDisabled() {
        final Document docDisabled = Document.createShell("");
        
        final String htmlNoCharset = "<html>\n" +
                                        " <head></head>\n" +
                                        " <body></body>\n" +
                                        "</html>";
        assertEquals(htmlNoCharset, docDisabled.toString());
        assertNull(docDisabled.select("meta[charset]").first());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateDisabledNoChanges
    public void testMetaCharsetUpdateDisabledNoChanges() {
        final Document doc = createHtmlDocument("dontTouch");
        
        final String htmlCharset = "<html>\n" +
                                    " <head>\n" +
                                    "  <meta charset=\"dontTouch\">\n" +
                                    "  <meta name=\"charset\" content=\"dontTouch\">\n" +
                                    " </head>\n" +
                                    " <body></body>\n" +
                                    "</html>";
        assertEquals(htmlCharset, doc.toString());
        
        Element selectedElement = doc.select("meta[charset]").first();
        assertNotNull(selectedElement);
        assertEquals("dontTouch", selectedElement.attr("charset"));
        
        selectedElement = doc.select("meta[name=charset]").first();
        assertNotNull(selectedElement);
        assertEquals("dontTouch", selectedElement.attr("content"));
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateEnabledAfterCharsetChange
    public void testMetaCharsetUpdateEnabledAfterCharsetChange() {
        final Document doc = createHtmlDocument("dontTouch");
        doc.charset(Charset.forName(charsetUtf8));
        
        Element selectedElement = doc.select("meta[charset]").first();
        assertEquals(charsetUtf8, selectedElement.attr("charset"));
        assertTrue(doc.select("meta[name=charset]").isEmpty());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateCleanup
    public void testMetaCharsetUpdateCleanup() {
        final Document doc = createHtmlDocument("dontTouch");
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String htmlCharsetUTF8 = "<html>\n" +
                                        " <head>\n" +
                                        "  <meta charset=\"" + charsetUtf8 + "\">\n" +
                                        " </head>\n" +
                                        " <body></body>\n" +
                                        "</html>";
        
        assertEquals(htmlCharsetUTF8, doc.toString());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateXmlUtf8
    public void testMetaCharsetUpdateXmlUtf8() {
        final Document doc = createXmlDocument("1.0", "changeThis", true);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String xmlCharsetUTF8 = "<?xml version=\"1.0\" encoding=\"" + charsetUtf8 + "\">\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetUTF8, doc.toString());

        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetUtf8, doc.charset().name());
        assertEquals(charsetUtf8, selectedNode.attr("encoding"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateXmlIso8859
    public void testMetaCharsetUpdateXmlIso8859() {
        final Document doc = createXmlDocument("1.0", "changeThis", true);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetIso8859));
        
        final String xmlCharsetISO = "<?xml version=\"1.0\" encoding=\"" + charsetIso8859 + "\">\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetISO, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetIso8859, doc.charset().name());
        assertEquals(charsetIso8859, selectedNode.attr("encoding"));
        assertEquals(doc.charset(), doc.outputSettings().charset());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateXmlNoCharset
    public void testMetaCharsetUpdateXmlNoCharset() {
        final Document doc = createXmlDocument("1.0", "none", false);
        doc.updateMetaCharsetElement(true);
        doc.charset(Charset.forName(charsetUtf8));
        
        final String xmlCharsetUTF8 = "<?xml version=\"1.0\" encoding=\"" + charsetUtf8 + "\">\n" +
                                        "<root>\n" +
                                        " node\n" +
                                        "</root>";
        assertEquals(xmlCharsetUTF8, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals(charsetUtf8, selectedNode.attr("encoding"));
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateXmlDisabled
    public void testMetaCharsetUpdateXmlDisabled() {
        final Document doc = createXmlDocument("none", "none", false);
        
        final String xmlNoCharset = "<root>\n" +
                                    " node\n" +
                                    "</root>";
        assertEquals(xmlNoCharset, doc.toString());
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdateXmlDisabledNoChanges
    public void testMetaCharsetUpdateXmlDisabledNoChanges() {
        final Document doc = createXmlDocument("dontTouch", "dontTouch", true);
        
        final String xmlCharset = "<?xml version=\"dontTouch\" encoding=\"dontTouch\">\n" +
                                    "<root>\n" +
                                    " node\n" +
                                    "</root>";
        assertEquals(xmlCharset, doc.toString());
        
        XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
        assertEquals("dontTouch", selectedNode.attr("encoding"));
        assertEquals("dontTouch", selectedNode.attr("version"));
    }

// org.jsoup.nodes.DocumentTest::testMetaCharsetUpdatedDisabledPerDefault
    public void testMetaCharsetUpdatedDisabledPerDefault() {
        final Document doc = createHtmlDocument("none");
        assertFalse(doc.updateMetaCharsetElement());
    }

// org.jsoup.nodes.DocumentTest::testShiftJisRoundtrip
    public void testShiftJisRoundtrip() throws Exception {
        String input =
                "<html>"
                        +   "<head>"
                        +     "<meta http-equiv=\"content-type\" content=\"text/html; charset=Shift_JIS\" />"
                        +   "</head>"
                        +   "<body>"
                        +     "before&nbsp;after"
                        +   "</body>"
                        + "</html>";
        InputStream is = new ByteArrayInputStream(input.getBytes(Charset.forName("ASCII")));

        Document doc = Jsoup.parse(is, null, "http://example.com");
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        String output = new String(doc.html().getBytes(doc.outputSettings().charset()), doc.outputSettings().charset());

        assertFalse("Should not have contained a '?'.", output.contains("?"));
        assertTrue("Should have contained a '&#xa0;' or a '&nbsp;'.",
                output.contains("&#xa0;") || output.contains("&nbsp;"));
    }

// org.jsoup.parser.HtmlParserTest::parsesSimpleDocument
    @Test public void parsesSimpleDocument() {
        String html = "<html><head><title>First!</title></head><body><p>First post! <img src=\"foo.png\" /></p></body></html>";
        Document doc = Jsoup.parse(html);
        
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
        Element img = p.child(0);
        assertEquals("foo.png", img.attr("src"));
        assertEquals("img", img.tagName());
    }

// org.jsoup.parser.HtmlParserTest::parsesRoughAttributes
    @Test public void parsesRoughAttributes() {
        String html = "<html><head><title>First!</title></head><body><p class=\"foo > bar\">First post! <img src=\"foo.png\" /></p></body></html>";
        Document doc = Jsoup.parse(html);

        
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
        assertEquals("foo > bar", p.attr("class"));
    }

// org.jsoup.parser.HtmlParserTest::parsesQuiteRoughAttributes
    @Test public void parsesQuiteRoughAttributes() {
        String html = "<p =a>One<a <p>Something</p>Else";
        
        Document doc = Jsoup.parse(html);
        assertEquals("<p =a>One<a <p>Something</a></p>\n" +
                "<a <p>Else</a>", doc.body().html());

        doc = Jsoup.parse("<p .....>");
        assertEquals("<p .....></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::parsesComments
    @Test public void parsesComments() {
        String html = "<html><head></head><body><img src=foo><!-- <table><tr><td></table> --><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);

        Element body = doc.body();
        Comment comment = (Comment) body.childNode(1); 
        assertEquals(" <table><tr><td></table> ", comment.getData());
        Element p = body.child(1);
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
    }

// org.jsoup.parser.HtmlParserTest::parsesUnterminatedComments
    @Test public void parsesUnterminatedComments() {
        String html = "<p>Hello<!-- <tr><td>";
        Document doc = Jsoup.parse(html);
        Element p = doc.getElementsByTag("p").get(0);
        assertEquals("Hello", p.text());
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
        Comment comment = (Comment) p.childNode(1);
        assertEquals(" <tr><td>", comment.getData());
    }

// org.jsoup.parser.HtmlParserTest::dropsUnterminatedTag
    @Test public void dropsUnterminatedTag() {
        
        String h1 = "<p";
        Document doc = Jsoup.parse(h1);
        assertEquals(0, doc.getElementsByTag("p").size());
        assertEquals("", doc.text());

        String h2 = "<div id=1<p id='2'";
        doc = Jsoup.parse(h2);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.HtmlParserTest::dropsUnterminatedAttribute
    @Test public void dropsUnterminatedAttribute() {
        
        String h1 = "<p id=\"foo";
        Document doc = Jsoup.parse(h1);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.HtmlParserTest::parsesUnterminatedTextarea
    @Test public void parsesUnterminatedTextarea() {
        
        Document doc = Jsoup.parse("<body><p><textarea>one<p>two");
        Element t = doc.select("textarea").first();
        assertEquals("one", t.text());
        assertEquals("two", doc.select("p").get(1).text());
    }

// org.jsoup.parser.HtmlParserTest::parsesUnterminatedOption
    @Test public void parsesUnterminatedOption() {
        
        Document doc = Jsoup.parse("<body><p><select><option>One<option>Two</p><p>Three</p>");
        Elements options = doc.select("option");
        assertEquals(2, options.size());
        assertEquals("One", options.first().text());
        assertEquals("TwoThree", options.last().text());
    }

// org.jsoup.parser.HtmlParserTest::testSpaceAfterTag
    @Test public void testSpaceAfterTag() {
        Document doc = Jsoup.parse("<div > <a name=\"top\"></a ><p id=1 >Hello</p></div>");
        assertEquals("<div> <a name=\"top\"></a><p id=\"1\">Hello</p></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::createsDocumentStructure
    @Test public void createsDocumentStructure() {
        String html = "<meta name=keywords /><link rel=stylesheet /><title>jsoup</title><p>Hello world</p>";
        Document doc = Jsoup.parse(html);
        Element head = doc.head();
        Element body = doc.body();

        assertEquals(1, doc.children().size()); 
        assertEquals(2, doc.child(0).children().size()); 
        assertEquals(3, head.children().size());
        assertEquals(1, body.children().size());

        assertEquals("keywords", head.getElementsByTag("meta").get(0).attr("name"));
        assertEquals(0, body.getElementsByTag("meta").size());
        assertEquals("jsoup", doc.title());
        assertEquals("Hello world", body.text());
        assertEquals("Hello world", body.children().get(0).text());
    }

// org.jsoup.parser.HtmlParserTest::createsStructureFromBodySnippet
    @Test public void createsStructureFromBodySnippet() {
        
        
        String html = "foo <b>bar</b> baz";
        Document doc = Jsoup.parse(html);
        assertEquals("foo bar baz", doc.text());

    }

// org.jsoup.parser.HtmlParserTest::handlesEscapedData
    @Test public void handlesEscapedData() {
        String html = "<div title='Surf &amp; Turf'>Reef &amp; Beef</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementsByTag("div").get(0);

        assertEquals("Surf & Turf", div.attr("title"));
        assertEquals("Reef & Beef", div.text());
    }

// org.jsoup.parser.HtmlParserTest::handlesDataOnlyTags
    @Test public void handlesDataOnlyTags() {
        String t = "<style>font-family: bold</style>";
        List<Element> tels = Jsoup.parse(t).getElementsByTag("style");
        assertEquals("font-family: bold", tels.get(0).data());
        assertEquals("", tels.get(0).text());

        String s = "<p>Hello</p><script>obj.insert('<a rel=\"none\" />');\ni++;</script><p>There</p>";
        Document doc = Jsoup.parse(s);
        assertEquals("Hello There", doc.text());
        assertEquals("obj.insert('<a rel=\"none\" />');\ni++;", doc.data());
    }

// org.jsoup.parser.HtmlParserTest::handlesTextAfterData
    @Test public void handlesTextAfterData() {
        String h = "<html><body>pre <script>inner</script> aft</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body>pre <script>inner</script> aft</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTextArea
    @Test public void handlesTextArea() {
        Document doc = Jsoup.parse("<textarea>Hello</textarea>");
        Elements els = doc.select("textarea");
        assertEquals("Hello", els.text());
        assertEquals("Hello", els.val());
    }

// org.jsoup.parser.HtmlParserTest::preservesSpaceInTextArea
    @Test public void preservesSpaceInTextArea() {
        
        Document doc = Jsoup.parse("<textarea>\n\tOne\n\tTwo\n\tThree\n</textarea>");
        String expect = "One\n\tTwo\n\tThree"; 
        Element el = doc.select("textarea").first();
        assertEquals(expect, el.text());
        assertEquals(expect, el.val());
        assertEquals(expect, el.html());
        assertEquals("<textarea>\n\t" + expect + "\n</textarea>", el.outerHtml()); 
    }

// org.jsoup.parser.HtmlParserTest::preservesSpaceInScript
    @Test public void preservesSpaceInScript() {
        
        Document doc = Jsoup.parse("<script>\nOne\n\tTwo\n\tThree\n</script>");
        String expect = "\nOne\n\tTwo\n\tThree\n";
        Element el = doc.select("script").first();
        assertEquals(expect, el.data());
        assertEquals("One\n\tTwo\n\tThree", el.html());
        assertEquals("<script>" + expect + "</script>", el.outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::doesNotCreateImplicitLists
    @Test public void doesNotCreateImplicitLists() {
        
        String h = "<li>Point one<li>Point two";
        Document doc = Jsoup.parse(h);
        Elements ol = doc.select("ul"); 
        assertEquals(0, ol.size());
        Elements lis = doc.select("li");
        assertEquals(2, lis.size());
        assertEquals("body", lis.first().parent().tagName());

        
        String h2 = "<ol><li><p>Point the first<li><p>Point the second";
        Document doc2 = Jsoup.parse(h2);

        assertEquals(0, doc2.select("ul").size());
        assertEquals(1, doc2.select("ol").size());
        assertEquals(2, doc2.select("ol li").size());
        assertEquals(2, doc2.select("ol li p").size());
        assertEquals(1, doc2.select("ol li").get(0).children().size()); 
    }

// org.jsoup.parser.HtmlParserTest::discardsNakedTds
    @Test public void discardsNakedTds() {
        
        String h = "<td>Hello<td><p>There<p>now";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello<p>There</p><p>now</p>", TextUtil.stripNewlines(doc.body().html()));
        
    }

// org.jsoup.parser.HtmlParserTest::handlesNestedImplicitTable
    @Test public void handlesNestedImplicitTable() {
        Document doc = Jsoup.parse("<table><td>1</td></tr> <td>2</td></tr> <td> <table><td>3</td> <td>4</td></table> <tr><td>5</table>");
        assertEquals("<table><tbody><tr><td>1</td></tr> <tr><td>2</td></tr> <tr><td> <table><tbody><tr><td>3</td> <td>4</td></tr></tbody></table> </td></tr><tr><td>5</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesWhatWgExpensesTableExample
    @Test public void handlesWhatWgExpensesTableExample() {
        
        Document doc = Jsoup.parse("<table> <colgroup> <col> <colgroup> <col> <col> <col> <thead> <tr> <th> <th>2008 <th>2007 <th>2006 <tbody> <tr> <th scope=rowgroup> Research and development <td> $ 1,109 <td> $ 782 <td> $ 712 <tr> <th scope=row> Percentage of net sales <td> 3.4% <td> 3.3% <td> 3.7% <tbody> <tr> <th scope=rowgroup> Selling, general, and administrative <td> $ 3,761 <td> $ 2,963 <td> $ 2,433 <tr> <th scope=row> Percentage of net sales <td> 11.6% <td> 12.3% <td> 12.6% </table>");
        assertEquals("<table> <colgroup> <col> </colgroup><colgroup> <col> <col> <col> </colgroup><thead> <tr> <th> </th><th>2008 </th><th>2007 </th><th>2006 </th></tr></thead><tbody> <tr> <th scope=\"rowgroup\"> Research and development </th><td> $ 1,109 </td><td> $ 782 </td><td> $ 712 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 3.4% </td><td> 3.3% </td><td> 3.7% </td></tr></tbody><tbody> <tr> <th scope=\"rowgroup\"> Selling, general, and administrative </th><td> $ 3,761 </td><td> $ 2,963 </td><td> $ 2,433 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 11.6% </td><td> 12.3% </td><td> 12.6% </td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTbodyTable
    @Test public void handlesTbodyTable() {
        Document doc = Jsoup.parse("<html><head></head><body><table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table></body></html>");
        assertEquals("<table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesImplicitCaptionClose
    @Test public void handlesImplicitCaptionClose() {
        Document doc = Jsoup.parse("<table><caption>A caption<td>One<td>Two");
        assertEquals("<table><caption>A caption</caption><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::noTableDirectInTable
    @Test public void noTableDirectInTable() {
        Document doc = Jsoup.parse("<table> <td>One <td><table><td>Two</table> <table><td>Three");
        assertEquals("<table> <tbody><tr><td>One </td><td><table><tbody><tr><td>Two</td></tr></tbody></table> <table><tbody><tr><td>Three</td></tr></tbody></table></td></tr></tbody></table>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::ignoresDupeEndTrTag
    @Test public void ignoresDupeEndTrTag() {
        Document doc = Jsoup.parse("<table><tr><td>One</td><td><table><tr><td>Two</td></tr></tr></table></td><td>Three</td></tr></table>"); 
        assertEquals("<table><tbody><tr><td>One</td><td><table><tbody><tr><td>Two</td></tr></tbody></table></td><td>Three</td></tr></tbody></table>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesBaseTags
    @Test public void handlesBaseTags() {
        
        String h = "<a href=1>#</a><base href='/2/'><a href='3'>#</a><base href='http://bar'><a href=/4>#</a>";
        Document doc = Jsoup.parse(h, "http://foo/");
        assertEquals("http://foo/2/", doc.baseUri()); 

        Elements anchors = doc.getElementsByTag("a");
        assertEquals(3, anchors.size());

        assertEquals("http://foo/2/", anchors.get(0).baseUri());
        assertEquals("http://foo/2/", anchors.get(1).baseUri());
        assertEquals("http://foo/2/", anchors.get(2).baseUri());

        assertEquals("http://foo/2/1", anchors.get(0).absUrl("href"));
        assertEquals("http://foo/2/3", anchors.get(1).absUrl("href"));
        assertEquals("http://foo/4", anchors.get(2).absUrl("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesProtocolRelativeUrl
    @Test public void handlesProtocolRelativeUrl() {
        String base = "https://example.com/";
        String html = "<img src='//example.net/img.jpg'>";
        Document doc = Jsoup.parse(html, base);
        Element el = doc.select("img").first();
        assertEquals("https://example.net/img.jpg", el.absUrl("src"));
    }

// org.jsoup.parser.HtmlParserTest::handlesCdata
    @Test public void handlesCdata() {
        
        String h = "<div id=1><![CDATA[<html>\n<foo><&amp;]]></div>"; 
        Document doc = Jsoup.parse(h);
        Element div = doc.getElementById("1");
        assertEquals("<html> <foo><&amp;", div.text());
        assertEquals(0, div.children().size());
        assertEquals(1, div.childNodeSize()); 
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedCdataAtEOF
    @Test public void handlesUnclosedCdataAtEOF() {
        
        String h = "<![CDATA[]]";
        Document doc = Jsoup.parse(h);
        assertEquals(1, doc.body().childNodeSize());
    }

// org.jsoup.parser.HtmlParserTest::handlesInvalidStartTags
    @Test public void handlesInvalidStartTags() {
        String h = "<div>Hello < There <&amp;></div>"; 
        Document doc = Jsoup.parse(h);
        assertEquals("Hello < There <&>", doc.select("div").first().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownTags
    @Test public void handlesUnknownTags() {
        String h = "<div><foo title=bar>Hello<foo title=qux>there</foo></div>";
        Document doc = Jsoup.parse(h);
        Elements foos = doc.select("foo");
        assertEquals(2, foos.size());
        assertEquals("bar", foos.first().attr("title"));
        assertEquals("qux", foos.last().attr("title"));
        assertEquals("there", foos.last().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownInlineTags
    @Test public void handlesUnknownInlineTags() {
        String h = "<p><cust>Test</cust></p><p><cust><cust>Test</cust></cust></p>";
        Document doc = Jsoup.parseBodyFragment(h);
        String out = doc.body().html();
        assertEquals(h, TextUtil.stripNewlines(out));
    }

// org.jsoup.parser.HtmlParserTest::parsesBodyFragment
    @Test public void parsesBodyFragment() {
        String h = "<!-- comment --><p><a href='foo'>One</a></p>";
        Document doc = Jsoup.parseBodyFragment(h, "http://example.com");
        assertEquals("<body><!-- comment --><p><a href=\"foo\">One</a></p></body>", TextUtil.stripNewlines(doc.body().outerHtml()));
        assertEquals("http://example.com/foo", doc.select("a").first().absUrl("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownNamespaceTags
    @Test public void handlesUnknownNamespaceTags() {
        
        String h = "<foo:bar id='1' /><abc:def id=2>Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>";
        Document doc = Jsoup.parse(h);
        assertEquals("<foo:bar id=\"1\" /><abc:def id=\"2\">Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyBlocks
    @Test public void handlesKnownEmptyBlocks() {
        
        String h = "<div id='1' /><script src='/foo' /><div id=2><img /><img></div><a id=3 /><i /><foo /><foo>One</foo> <hr /> hr text <hr> hr text two";
        Document doc = Jsoup.parse(h);
        assertEquals("<div id=\"1\"></div><script src=\"/foo\"></script><div id=\"2\"><img><img></div><a id=\"3\"></a><i></i><foo /><foo>One</foo> <hr> hr text <hr> hr text two", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusAtAttributeEnd
    @Test public void handlesSolidusAtAttributeEnd() {
        
        String h = "<a href=/>link</a>";
        Document doc = Jsoup.parse(h);
        assertEquals("<a href=\"/\">link</a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesMultiClosingBody
    @Test public void handlesMultiClosingBody() {
        String h = "<body><p>Hello</body><p>there</p></body></body></html><p>now";
        Document doc = Jsoup.parse(h);
        assertEquals(3, doc.select("p").size());
        assertEquals(3, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedDefinitionLists
    @Test public void handlesUnclosedDefinitionLists() {
        
        String h = "<dt>Foo<dd>Bar<dt>Qux<dd>Zug";
        Document doc = Jsoup.parse(h);
        assertEquals(0, doc.select("dl").size()); 
        assertEquals(4, doc.select("dt, dd").size());
        Elements dts = doc.select("dt");
        assertEquals(2, dts.size());
        assertEquals("Zug", dts.get(1).nextElementSibling().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesBlocksInDefinitions
    @Test public void handlesBlocksInDefinitions() {
        
        String h = "<dl><dt><div id=1>Term</div></dt><dd><div id=2>Def</div></dd></dl>";
        Document doc = Jsoup.parse(h);
        assertEquals("dt", doc.select("#1").first().parent().tagName());
        assertEquals("dd", doc.select("#2").first().parent().tagName());
        assertEquals("<dl><dt><div id=\"1\">Term</div></dt><dd><div id=\"2\">Def</div></dd></dl>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesFrames
    @Test public void handlesFrames() {
        String h = "<html><head><script></script><noscript></noscript></head><frameset><frame src=foo></frame><frame src=foo></frameset></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\"><frame src=\"foo\"></frameset></html>",
                TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::ignoresContentAfterFrameset
    @Test public void ignoresContentAfterFrameset() {
        String h = "<html><head><title>One</title></head><frameset><frame /><frame /></frameset><table></table></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title>One</title></head><frameset><frame><frame></frameset></html>", TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::handlesJavadocFont
    @Test public void handlesJavadocFont() {
        String h = "<TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("Deprecated", a.text());
        assertEquals("font", a.child(0).tagName());
        assertEquals("b", a.child(0).child(0).tagName());
    }

// org.jsoup.parser.HtmlParserTest::handlesBaseWithoutHref
    @Test public void handlesBaseWithoutHref() {
        String h = "<head><base target='_blank'></head><body><a href=/foo>Test</a></body>";
        Document doc = Jsoup.parse(h, "http://example.com/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://example.com/foo", a.attr("abs:href"));
    }

// org.jsoup.parser.HtmlParserTest::normalisesDocument
    @Test public void normalisesDocument() {
        String h = "<!doctype html>One<html>Two<head>Three<link></head>Four<body>Five </body>Six </html>Seven ";
        Document doc = Jsoup.parse(h);
        assertEquals("<!doctype html><html><head></head><body>OneTwoThree<link>FourFive Six Seven </body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesEmptyDocument
    @Test public void normalisesEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertEquals("<html><head></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesHeadlessBody
    @Test public void normalisesHeadlessBody() {
        Document doc = Jsoup.parse("<html><body><span class=\"foo\">bar</span>");
        assertEquals("<html><head></head><body><span class=\"foo\">bar</span></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisedBodyAfterContent
    @Test public void normalisedBodyAfterContent() {
        Document doc = Jsoup.parse("<font face=Arial><body class=name><div>One</div></body></font>");
        assertEquals("<html><head></head><body class=\"name\"><font face=\"Arial\"><div>One</div></font></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::findsCharsetInMalformedMeta
    @Test public void findsCharsetInMalformedMeta() {
        String h = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>";
        
        Document doc = Jsoup.parse(h);
        assertEquals("gb2312", doc.select("meta").attr("charset"));
    }

// org.jsoup.parser.HtmlParserTest::testHgroup
    @Test public void testHgroup() {
        
        Document doc = Jsoup.parse("<h1>Hello <h2>There <hgroup><h1>Another<h2>headline</hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup>");
        assertEquals("<h1>Hello </h1><h2>There <hgroup><h1>Another</h1><h2>headline</h2></hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup></h2>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def>There</abc-def>");
        assertEquals("<abc_def id=\"1\">Hello</abc_def> <abc-def>There</abc-def>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testHeaderContents
    @Test public void testHeaderContents() {
        
        
        Document doc = Jsoup.parse("<h1>Hello <div>There</div> now</h1> <h2>More <h3>Content</h3></h2>");
        assertEquals("<h1>Hello <div>There</div> now</h1> <h2>More </h2><h3>Content</h3>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testSpanContents
    @Test public void testSpanContents() {
        
        Document doc = Jsoup.parse("<span>Hello <div>there</div> <span>now</span></span>");
        assertEquals("<span>Hello <div>there</div> <span>now</span></span>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testNoImagesInNoScriptInHead
    @Test public void testNoImagesInNoScriptInHead() {
        
        Document doc = Jsoup.parse("<html><head><noscript><img src='foo'></noscript></head><body><p>Hello</p></body></html>");
        assertEquals("<html><head><noscript>&lt;img src=\"foo\"&gt;</noscript></head><body><p>Hello</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::testAFlowContents
    @Test public void testAFlowContents() {
        
        Document doc = Jsoup.parse("<a>Hello <div>there</div> <span>now</span></a>");
        assertEquals("<a>Hello <div>there</div> <span>now</span></a>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testFontFlowContents
    @Test public void testFontFlowContents() {
        
        Document doc = Jsoup.parse("<font>Hello <div>there</div> <span>now</span></font>");
        assertEquals("<font>Hello <div>there</div> <span>now</span></font>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBI
    @Test public void handlesMisnestedTagsBI() {
        
        String h = "<p>1<b>2<i>3</b>4</i>5</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p>1<b>2<i>3</i></b><i>4</i>5</p>", doc.body().html());
        
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBP
    @Test public void handlesMisnestedTagsBP() {
        
        String h = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnexpectedMarkupInTables
    @Test public void handlesUnexpectedMarkupInTables() {
        
        
        String h = "<table><b><tr><td>aaa</td></tr>bbb</table>ccc";
        Document doc = Jsoup.parse(h);
        assertEquals("<b></b><b>bbb</b><table><tbody><tr><td>aaa</td></tr></tbody></table><b>ccc</b>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedFormattingElements
    @Test public void handlesUnclosedFormattingElements() {
        
        String h = "<!DOCTYPE html>\n" +
                "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
                "<p>X\n" +
                "<p><b><b class=x><b>X\n" +
                "<p></b></b></b></b></b></b>X";
        Document doc = Jsoup.parse(h);
        doc.outputSettings().indentAmount(0);
        String want = "<!doctype html>\n" +
                "<html>\n" +
                "<head></head>\n" +
                "<body>\n" +
                "<p><b class=\"x\"><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b><b><b class=\"x\"><b>X </b></b></b></b></b></b></b></b></p>\n" +
                "<p>X</p>\n" +
                "</body>\n" +
                "</html>";
        assertEquals(want, doc.html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedAnchors
    @Test public void handlesUnclosedAnchors() {
        String h = "<a href='http://example.com/'>Link<p>Error link</a>";
        Document doc = Jsoup.parse(h);
        String want = "<a href=\"http://example.com/\">Link</a>\n<p><a href=\"http://example.com/\">Error link</a></p>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElements
    @Test public void reconstructFormattingElements() {
        
        String h = "<p><b class=one>One <i>Two <b>Three</p><p>Hello</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p><b class=\"one\">One <i>Two <b>Three</b></i></b></p>\n<p><b class=\"one\"><i><b>Hello</b></i></b></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElementsInTable
    @Test public void reconstructFormattingElementsInTable() {
        
        
        String h = "<p><b>One</p> <table><tr><td><p><i>Three<p>Four</i></td></tr></table> <p>Five</p>";
        Document doc = Jsoup.parse(h);
        String want = "<p><b>One</b></p>\n" +
                "<b> \n" +
                " <table>\n" +
                "  <tbody>\n" +
                "   <tr>\n" +
                "    <td><p><i>Three</i></p><p><i>Four</i></p></td>\n" +
                "   </tr>\n" +
                "  </tbody>\n" +
                " </table> <p>Five</p></b>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::commentBeforeHtml
    @Test public void commentBeforeHtml() {
        String h = "<!-- comment --><!-- comment 2 --><p>One</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<!-- comment --><!-- comment 2 --><html><head></head><body><p>One</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::emptyTdTag
    @Test public void emptyTdTag() {
        String h = "<table><tr><td>One</td><td id='2' /></tr></table>";
        Document doc = Jsoup.parse(h);
        assertEquals("<td>One</td>\n<td id=\"2\"></td>", doc.select("tr").first().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusInA
    @Test public void handlesSolidusInA() {
        
        String h = "<a class=lp href=/lib/14160711/>link text</a>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("link text", a.text());
        assertEquals("/lib/14160711/", a.attr("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesSpanInTbody
    @Test public void handlesSpanInTbody() {
        
        String h = "<table><tbody><span class='1'><tr><td>One</td></tr><tr><td>Two</td></tr></span></tbody></table>";
        Document doc = Jsoup.parse(h);
        assertEquals(doc.select("span").first().children().size(), 0); 
        assertEquals(doc.select("table").size(), 1); 
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitleAtEof
    @Test public void handlesUnclosedTitleAtEof() {
        assertEquals("Data", Jsoup.parse("<title>Data").title());
        assertEquals("Data<", Jsoup.parse("<title>Data<").title());
        assertEquals("Data</", Jsoup.parse("<title>Data</").title());
        assertEquals("Data</t", Jsoup.parse("<title>Data</t").title());
        assertEquals("Data</ti", Jsoup.parse("<title>Data</ti").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title>").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title >").title());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitle
    @Test public void handlesUnclosedTitle() {
        Document one = Jsoup.parse("<title>One <b>Two <b>Three</TITLE><p>Test</p>"); 
        assertEquals("One <b>Two <b>Three", one.title());
        assertEquals("Test", one.select("p").first().text());

        Document two = Jsoup.parse("<title>One<b>Two <p>Test</p>"); 
        assertEquals("One", two.title());
        assertEquals("<b>Two <p>Test</p></b>", two.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedScriptAtEof
    @Test public void handlesUnclosedScriptAtEof() {
        assertEquals("Data", Jsoup.parse("<script>Data").select("script").first().data());
        assertEquals("Data<", Jsoup.parse("<script>Data<").select("script").first().data());
        assertEquals("Data</sc", Jsoup.parse("<script>Data</sc").select("script").first().data());
        assertEquals("Data</-sc", Jsoup.parse("<script>Data</-sc").select("script").first().data());
        assertEquals("Data</sc-", Jsoup.parse("<script>Data</sc-").select("script").first().data());
        assertEquals("Data</sc--", Jsoup.parse("<script>Data</sc--").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script>").select("script").first().data());
        assertEquals("Data</script", Jsoup.parse("<script>Data</script").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script ").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"p").select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedRawtextAtEof
    @Test public void handlesUnclosedRawtextAtEof() {
        assertEquals("Data", Jsoup.parse("<style>Data").select("style").first().data());
        assertEquals("Data</st", Jsoup.parse("<style>Data</st").select("style").first().data());
        assertEquals("Data", Jsoup.parse("<style>Data</style>").select("style").first().data());
        assertEquals("Data</style", Jsoup.parse("<style>Data</style").select("style").first().data());
        assertEquals("Data</-style", Jsoup.parse("<style>Data</-style").select("style").first().data());
        assertEquals("Data</style-", Jsoup.parse("<style>Data</style-").select("style").first().data());
        assertEquals("Data</style--", Jsoup.parse("<style>Data</style--").select("style").first().data());
    }

// org.jsoup.parser.HtmlParserTest::noImplicitFormForTextAreas
    @Test public void noImplicitFormForTextAreas() {
        
        Document doc = Jsoup.parse("<textarea>One</textarea>");
        assertEquals("<textarea>One</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesEscapedScript
    @Test public void handlesEscapedScript() {
        Document doc = Jsoup.parse("<script><!-- one <script>Blah</script> --></script>");
        assertEquals("<!-- one <script>Blah</script> -->", doc.select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handles0CharacterAsText
    @Test public void handles0CharacterAsText() {
        Document doc = Jsoup.parse("0<p>0</p>");
        assertEquals("0\n<p>0</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInData
    @Test public void handlesNullInData() {
        Document doc = Jsoup.parse("<p id=\u0000>Blah \u0000</p>");
        assertEquals("<p id=\"\uFFFD\">Blah \u0000</p>", doc.body().html()); 
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInComments
    @Test public void handlesNullInComments() {
        Document doc = Jsoup.parse("<body><!-- \u0000 \u0000 -->");
        assertEquals("<!-- \uFFFD \uFFFD -->", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNewlinesAndWhitespaceInTag
    @Test public void handlesNewlinesAndWhitespaceInTag() {
        Document doc = Jsoup.parse("<a \n href=\"one\" \r\n id=\"two\" \f >");
        assertEquals("<a href=\"one\" id=\"two\"></a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesWhitespaceInoDocType
    @Test public void handlesWhitespaceInoDocType() {
        String html = "<!DOCTYPE html\r\n" +
                "      PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\r\n" +
                "      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
        Document doc = Jsoup.parse(html);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">", doc.childNode(0).outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::tracksErrorsWhenRequested
    @Test public void tracksErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(500);
        Document doc = Jsoup.parse(html, "http://example.com", parser);
        
        List<ParseError> errors = parser.getErrors();
        assertEquals(5, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
        assertEquals("50: Self closing flag not acknowledged", errors.get(3).toString());
        assertEquals("61: Unexpectedly reached end of file (EOF) in input state [TagName]", errors.get(4).toString());
    }

// org.jsoup.parser.HtmlParserTest::tracksLimitedErrorsWhenRequested
    @Test public void tracksLimitedErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(3);
        Document doc = parser.parseInput(html, "http://example.com");

        List<ParseError> errors = parser.getErrors();
        assertEquals(3, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
    }

// org.jsoup.parser.HtmlParserTest::noErrorsByDefault
    @Test public void noErrorsByDefault() {
        String html = "<p>One</p href='no'>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, "http://example.com", parser);

        List<ParseError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

// org.jsoup.parser.HtmlParserTest::handlesCommentsInTable
    @Test public void handlesCommentsInTable() {
        String html = "<table><tr><td>text</td><!-- Comment --></tr></table>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<html><head></head><body><table><tbody><tr><td>text</td><!-- Comment --></tr></tbody></table></body></html>", TextUtil.stripNewlines(node.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesQuotesInCommentsInScripts
    @Test public void handlesQuotesInCommentsInScripts() {
        String html = "<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  
                "</script>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  
                "</script>", node.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handleNullContextInParseFragment
    @Test public void handleNullContextInParseFragment() {
        String html = "<ol><li>One</li></ol><p>Two</p>";
        List<Node> nodes = Parser.parseFragment(html, null, "http://example.com/");
        assertEquals(1, nodes.size()); 
        assertEquals("html", nodes.get(0).nodeName());
        assertEquals("<html> <head></head> <body> <ol> <li>One</li> </ol> <p>Two</p> </body> </html>", StringUtil.normaliseWhitespace(nodes.get(0).outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::doesNotFindShortestMatchingEntity
    @Test public void doesNotFindShortestMatchingEntity() {
        
        
        String html = "One &clubsuite; &clubsuit;";
        Document doc = Jsoup.parse(html);
        assertEquals(StringUtil.normaliseWhitespace("One &amp;clubsuite; ♣"), doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::relaxedBaseEntityMatchAndStrictExtendedMatch
    @Test public void relaxedBaseEntityMatchAndStrictExtendedMatch() {
        
        String html = "&amp &quot &reg &icy &hopf &icy; &hopf;";
        Document doc = Jsoup.parse(html);
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended).charset("ascii"); 
        assertEquals("&amp; \" &reg; &amp;icy &amp;hopf &icy; &hopf;", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesXmlDeclarationAsBogusComment
    @Test public void handlesXmlDeclarationAsBogusComment() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body>";
        Document doc = Jsoup.parse(html);
        assertEquals("<!--?xml encoding='UTF-8' ?--> <html> <head></head> <body> One </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTagsInTextarea
    @Test public void handlesTagsInTextarea() {
        String html = "<textarea><p>Jsoup</p></textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("<textarea>&lt;p&gt;Jsoup&lt;/p&gt;</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::createsFormElements
    @Test public void createsFormElements() {
        String html = "<body><form><input id=1><input id=2></form></body>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());
    }

// org.jsoup.parser.HtmlParserTest::associatedFormControlsWithDisjointForms
    @Test public void associatedFormControlsWithDisjointForms() {
        
        String html = "<table><tr><form><input type=hidden id=1><td><input type=text id=2></td><tr></table>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());

        assertEquals("<table><tbody><tr><form></form><input type=\"hidden\" id=\"1\"><td><input type=\"text\" id=\"2\"></td></tr><tr></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesInputInTable
    @Test public void handlesInputInTable() {
        String h = "<body>\n" +
                "<input type=\"hidden\" name=\"a\" value=\"\">\n" +
                "<table>\n" +
                "<input type=\"hidden\" name=\"b\" value=\"\" />\n" +
                "</table>\n" +
                "</body>";
        Document doc = Jsoup.parse(h);
        assertEquals(1, doc.select("table input").size());
        assertEquals(2, doc.select("input").size());
    }

// org.jsoup.parser.HtmlParserTest::convertsImageToImg
    @Test public void convertsImageToImg() {
        
        String h = "<body><image><svg><image /></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<img>\n<svg>\n <image />\n</svg>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesInvalidDoctypes
    @Test public void handlesInvalidDoctypes() {
        
        Document doc = Jsoup.parse("<!DOCTYPE>");
        assertEquals(
                "<!doctype> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE><html><p>Foo</p></html>");
        assertEquals(
                "<!doctype> <html> <head></head> <body> <p>Foo</p> </body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE \u0000>");
        assertEquals(
                "<!doctype �> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesManyChildren
    @Test public void handlesManyChildren() {
        
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<br>");
        }
        
        
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");
        
        
        assertEquals(50000, doc.body().childNodeSize());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

// org.jsoup.parser.HtmlParserTest::testInvalidTableContents
    public void testInvalidTableContents() throws IOException {
        File in = ParseTest.getFile("/htmltests/table-invalid-elements.html");
        Document doc = Jsoup.parse(in, "UTF-8");
        doc.outputSettings().prettyPrint(true);
        String rendered = doc.toString();
        int endOfEmail = rendered.indexOf("Comment");
        int guarantee = rendered.indexOf("Why am I here?");
        assertTrue("Comment not found", endOfEmail > -1);
        assertTrue("Search text not found", guarantee > -1);
        assertTrue("Search text did not come after comment", guarantee > endOfEmail);
    }

// org.jsoup.parser.HtmlParserTest::testNormalisesIsIndex
    @Test public void testNormalisesIsIndex() {
        Document doc = Jsoup.parse("<body><isindex action='/submit'></body>");
        String html = doc.outerHtml();
        assertEquals("<form action=\"/submit\"> <hr> <label>This is a searchable index. Enter search keywords: <input name=\"isindex\"></label> <hr> </form>",
                StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testReinsertionModeForThCelss
    @Test public void testReinsertionModeForThCelss() {
        String body = "<body> <table> <tr> <th> <table><tr><td></td></tr></table> <div> <table><tr><td></td></tr></table> </div> <div></div> <div></div> <div></div> </th> </tr> </table> </body>";
        Document doc = Jsoup.parse(body);
        assertEquals(1, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::testUsingSingleQuotesInQueries
    @Test public void testUsingSingleQuotesInQueries() {
        String body = "<body> <div class='main'>hello</div></body>";
        Document doc = Jsoup.parse(body);
        Elements main = doc.select("div[class='main']");
        assertEquals("hello", main.text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSimpleXmlParse
    public void testSimpleXmlParse() {
        String xml = "<doc id=2 href='/bar'>Foo <br /><link>One</link><link>Two</link></doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc id=\"2\" href=\"/bar\">Foo <br /><link>One</link><link>Two</link></doc>",
                TextUtil.stripNewlines(doc.html()));
        assertEquals(doc.getElementById("2").absUrl("href"), "http://foo.com/bar");
    }

// org.jsoup.parser.XmlTreeBuilderTest::testPopToClose
    public void testPopToClose() {
        
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testCommentAndDocType
    public void testCommentAndDocType() {
        String xml = "<!DOCTYPE html><!-- a comment -->One <qux />Two";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<!DOCTYPE html><!-- a comment -->One <qux />Two",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToJsoupClass
    public void testSupplyParserToJsoupClass() {
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        Document doc = Jsoup.parse(xml, "http://foo.com/", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToConnection
    public void testSupplyParserToConnection() throws IOException {
        String xmlUrl = "http://direct.infohound.net/tools/jsoup-xml-test.xml";

        
        Document xmlDoc = Jsoup.connect(xmlUrl).parser(Parser.xmlParser()).get();
        Document htmlDoc = Jsoup.connect(xmlUrl).parser(Parser.htmlParser()).get();
        Document autoXmlDoc = Jsoup.connect(xmlUrl).get(); 

        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(xmlDoc.html()));
        assertFalse(htmlDoc.equals(xmlDoc));
        assertEquals(xmlDoc, autoXmlDoc);
        assertEquals(1, htmlDoc.select("head").size()); 
        assertEquals(0, xmlDoc.select("head").size()); 
        assertEquals(0, autoXmlDoc.select("head").size()); 
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToDataStream
    public void testSupplyParserToDataStream() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-test.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://foo.com", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesNotForceSelfClosingKnownTags
    public void testDoesNotForceSelfClosingKnownTags() {
        
        Document htmlDoc = Jsoup.parse("<br>one</br>");
        assertEquals("<br>one\n<br>", htmlDoc.body().html());

        Document xmlDoc = Jsoup.parse("<br>one</br>", "", Parser.xmlParser());
        assertEquals("<br>one</br>", xmlDoc.html());
    }

// org.jsoup.parser.XmlTreeBuilderTest::handlesXmlDeclarationAsDeclaration
    @Test public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding='UTF-8' ?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlFragment
    @Test public void xmlFragment() {
        String xml = "<one src='/foo/' />Two<three><four /></three>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "http://example.com/");
        assertEquals(3, nodes.size());

        assertEquals("http://example.com/foo/", nodes.get(0).absUrl("src"));
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("Two", ((TextNode)nodes.get(1)).text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlParseDefaultsToHtmlOutputSyntax
    @Test public void xmlParseDefaultsToHtmlOutputSyntax() {
        Document doc = Jsoup.parse("x", "", Parser.xmlParser());
        assertEquals(Syntax.xml, doc.outputSettings().syntax());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesHandleEOFInTag
    public void testDoesHandleEOFInTag() {
        String html = "<img src=asdf onerror=\"alert(1)\" x=";
        Document xmlDoc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<img src=\"asdf\" onerror=\"alert(1)\" x=\"\" />", xmlDoc.html());
    }
