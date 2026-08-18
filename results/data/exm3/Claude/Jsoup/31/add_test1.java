// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesMultipleCommentsAndDeclarations() {
    String html = "<?xml version='1.0'?><!-- first --><!-- second --><root/>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<?xml version='1.0'?> <!-- first --> <!-- second --> <root />".
        StringUtil.normaliseWhitespace(doc.outerHtml()));
    assertEquals("#declaration", doc.childNode(0).nodeName());
    assertEquals("#comment", doc.childNode(1).nodeName());
    assertEquals(" first ", ((Comment)doc.childNode(1)).getData());
    assertEquals("#comment", doc.childNode(2).nodeName());
    assertEquals(" second ", ((Comment)doc.childNode(2)).getData());
}