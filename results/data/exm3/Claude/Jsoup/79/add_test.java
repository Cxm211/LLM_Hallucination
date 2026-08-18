// org/jsoup/nodes/TextNodeTest.java
@Test public void testLeafNodeChildNodesIsImmutable() {
    Document doc = Jsoup.parse("<p>Test</p>");
    Element p = doc.select("p").first();
    TextNode tn = (TextNode) p.childNode(0);
    List<Node> nodes = tn.childNodes();
    assertEquals(0, nodes.size());
    try {
        nodes.add(new TextNode("fail"));
        fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
        // expected
    }
}