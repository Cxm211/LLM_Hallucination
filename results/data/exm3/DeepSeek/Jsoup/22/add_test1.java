// org/jsoup/nodes/NodeTest.java
@Test public void testSiblingNodesForAllChildren() {
    Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div>");
    Element div = doc.select("div").first();
    List<Node> children = div.childNodes();
    for (int i = 0; i < children.size(); i++) {
        Node node = children.get(i);
        List<Node> siblings = node.siblingNodes();
        assertEquals(2, siblings.size());
        assertFalse(siblings.contains(node));
        int index = 0;
        for (int j = 0; j < children.size(); j++) {
            if (j != i) {
                assertEquals(children.get(j), siblings.get(index));
                index++;
            }
        }
        if (i > 0) {
            assertEquals(children.get(i-1), node.previousSibling());
        } else {
            assertNull(node.previousSibling());
        }
        Node nextSibling = node.nextSibling();
        if (i < children.size() - 1) {
            assertEquals(children.get(i+1), nextSibling);
        } else {
            assertNull(nextSibling);
        }
    }
    Element parent2 = Jsoup.parse("<div><p>Only</p></div>").select("div").first();
    Node onlyChild = parent2.childNodes().get(0);
    assertEquals(0, onlyChild.siblingNodes().size());
    assertNull(onlyChild.previousSibling());
    assertNull(onlyChild.nextSibling());
}
