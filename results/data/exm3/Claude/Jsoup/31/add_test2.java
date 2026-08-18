// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesEmptyComment() {
    String html = "<root><!--  --></root>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<root> <!--  --> </root>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    assertEquals(1, doc.childNode(0).childNodes().size());
    assertEquals("#comment", doc.childNode(0).childNode(0).nodeName());
    assertEquals("  ", ((Comment)doc.childNode(0).childNode(0)).getData());
}