// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesCommentWithoutClosingBracket() {
    String html = "<body>One</body><!-- comment";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<body> One </body> <!-- comment -->", StringUtil.normaliseWhitespace(doc.outerHtml()));
    assertEquals("#comment", doc.childNode(1).nodeName());
    assertEquals(" comment", ((Comment)doc.childNode(1)).getData());
}