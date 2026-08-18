// org/jsoup/helper/W3CDomTest.java
@Test
public void namespaceInheritanceWithoutPrefix() throws IOException {
    String html = "<html xmlns='http://www.w3.org/1999/xhtml'><body><div>Test</div></body></html>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "", Parser.xmlParser());

    org.jsoup.helper.W3CDom jDom = new org.jsoup.helper.W3CDom();
    Document doc = jDom.fromJsoup(jsoupDoc);

    Node htmlEl = doc.getChildNodes().item(0);
    assertEquals("http://www.w3.org/1999/xhtml", htmlEl.getNamespaceURI());

    Node body = htmlEl.getChildNodes().item(0);
    assertEquals("http://www.w3.org/1999/xhtml", body.getNamespaceURI());
    assertEquals("body", body.getLocalName());

    Node div = body.getChildNodes().item(0);
    assertEquals("http://www.w3.org/1999/xhtml", div.getNamespaceURI());
    assertEquals("div", div.getLocalName());
}