// org/jsoup/helper/W3CDomTest.java
@Test
public void namespaceScoping() throws IOException {
    String html = "<root><inner xmlns:b='http://example.com/b'><b:item>text</b:item></inner><b:leak>should not have namespace</b:leak></root>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "", org.jsoup.parser.Parser.xmlParser());
    Document doc = new org.jsoup.helper.W3CDom().fromJsoup(jsoupDoc);
    Node root = doc.getDocumentElement();
    Node inner = root.getFirstChild();
    Node item = inner.getFirstChild();
    assertEquals("http://example.com/b", item.getNamespaceURI());
    Node leak = inner.getNextSibling();
    assertNull(leak.getNamespaceURI());
}
