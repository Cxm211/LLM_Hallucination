// org/jsoup/helper/W3CDomTest.java
@Test public void handlesMixedDeclaredAndUndeclaredNamespaces() {
    String html = "<html xmlns:valid='http://example.com'><valid:tag>Content</valid:tag><invalid:tag>Other</invalid:tag></html>";
    org.jsoup.nodes.Document doc = Jsoup.parse(html);

    Document w3Doc = new W3CDom().fromJsoup(doc);
    Node htmlEl = w3Doc.getFirstChild();

    Node validTag = htmlEl.getFirstChild().getNextSibling().getFirstChild();
    assertEquals("http://example.com", validTag.getNamespaceURI());
    assertEquals("tag", validTag.getLocalName());

    Node invalidTag = validTag.getNextSibling();
    assertNull(invalidTag.getNamespaceURI());
    assertEquals("tag", invalidTag.getLocalName());
    assertEquals("invalid:tag", invalidTag.getNodeName());
}