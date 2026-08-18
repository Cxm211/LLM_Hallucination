// org/jsoup/nodes/TextNodeTest.java
@Test public void testLeafNodesChildNodeSizeIsZero() {
        Document doc = Jsoup.parse("<div>Hello there</div>");
        Element div = doc.select("div").first();
        TextNode tn = (TextNode) div.childNode(0);
        assertEquals(0, tn.childNodeSize());
    }