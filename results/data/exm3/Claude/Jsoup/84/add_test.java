// org/jsoup/helper/W3CDomTest.java
@Test public void handlesMultipleUndeclaredNamespaces() {
    String html = "<custom:tag><other:element>Text</other:element></custom:tag>";
    org.jsoup.nodes.Document doc = Jsoup.parse(html);

    Document w3Doc = new W3CDom().fromJsoup(doc);
    Node htmlEl = w3Doc.getFirstChild();

    Node customTag = htmlEl.getFirstChild().getNextSibling().getFirstChild();
    assertNull(customTag.getNamespaceURI());
    assertEquals("tag", customTag.getLocalName());
    assertEquals("custom:tag", customTag.getNodeName());

    Node otherElement = customTag.getFirstChild();
    assertNull(otherElement.getNamespaceURI());
    assertEquals("element", otherElement.getLocalName());
    assertEquals("other:element", otherElement.getNodeName());
}