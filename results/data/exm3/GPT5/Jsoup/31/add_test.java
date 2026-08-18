// org/jsoup/parser/XmlTreeBuilderTest.java::handlesDeclarationStartingWithBang
@Test public void handlesDeclarationStartingWithBang() {
        String html = "<!foo bar='qux'>";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<!foo bar='qux'>", StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
    }