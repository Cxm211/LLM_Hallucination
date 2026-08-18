// org/jsoup/helper/W3CDomTest.java
@Test public void handlesUndeclaredNamespaceWithMixedContent() {
    String html = "<fb:like>One<!-- comment --><script>alert(1);</script></fb:like>";
    org.jsoup.nodes.Document doc = Jsoup.parse(html);
    Document w3Doc = new W3CDom().fromJsoup(doc);
    Node htmlEl = w3Doc.getFirstChild();
    Node body = htmlEl.getFirstChild().getNextSibling();
    Node fbLike = body.getFirstChild();
    assertNull(fbLike.getNamespaceURI());
    assertEquals("like", fbLike.getLocalName());
    assertEquals("fb:like", fbLike.getNodeName());
    NodeList children = fbLike.getChildNodes();
    assertEquals(3, children.getLength());
    Node text = children.item(0);
    assertTrue(text instanceof Text);
    assertEquals("One", text.getTextContent());
    Node comment = children.item(1);
    assertTrue(comment instanceof Comment);
    assertEquals(" comment ", ((Comment)comment).getData());
    Node script = children.item(2);
    assertTrue(script instanceof Element);
    assertEquals("script", script.getLocalName());
    Node scriptChild = script.getFirstChild();
    assertTrue(scriptChild instanceof Text);
    assertEquals("alert(1);", scriptChild.getTextContent());
}
