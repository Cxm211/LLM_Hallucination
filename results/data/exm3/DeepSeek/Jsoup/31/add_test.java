// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesXmlDeclarationWithVersionAndEncoding() {
    String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><body>One</body>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("#declaration", doc.childNode(0).nodeName());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <body> One </body>",
        StringUtil.normaliseWhitespace(doc.outerHtml()));
}
