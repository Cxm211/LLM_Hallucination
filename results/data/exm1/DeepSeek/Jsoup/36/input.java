// buggy code
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

                if (foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)) { // need to re-decode
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
        if (doc == null) {
            // there are times where there is a spurious byte-order-mark at the start of the text. Shouldn't be present
            // in utf-8. If after decoding, there is a BOM, strip it; otherwise will cause the parser to go straight
            // into head mode
            if (docData.length() > 0 && docData.charAt(0) == 65279)
                docData = docData.substring(1);

            doc = parser.parseInput(docData, baseUri);
            doc.outputSettings().charset(charsetName);
        }
        return doc;
    }

    static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
                if (Charset.isSupported(charset)) return charset;
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset)) return charset;
                // if our advanced charset matching fails.... we just take the default
        }
        return null;
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
        Document htmlDoc = Jsoup.connect(xmlUrl).get();

        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(xmlDoc.html()));
        assertNotSame(htmlDoc, xmlDoc);
        assertEquals(1, htmlDoc.select("head").size()); 
        assertEquals(0, xmlDoc.select("head").size()); 
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
        assertEquals("<br />one\n<br />", htmlDoc.body().html());

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
