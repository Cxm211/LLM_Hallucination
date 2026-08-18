// org/jsoup/nodes/TextNodeTest.java
@Test public void testLeafNodeChildNodesMultipleCalls() {
    Document doc = Jsoup.parse("<span>Content</span>");
    Element span = doc.select("span").first();
    TextNode tn = (TextNode) span.childNode(0);
    List<Node> nodes1 = tn.childNodes();
    List<Node> nodes2 = tn.childNodes();
    assertEquals(0, nodes1.size());
    assertEquals(0, nodes2.size());
    assertSame(nodes1, nodes2);
}