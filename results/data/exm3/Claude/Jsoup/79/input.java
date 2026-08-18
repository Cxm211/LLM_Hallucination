// buggy function
    protected List<Node> ensureChildNodes() {
        throw new UnsupportedOperationException("Leaf Nodes do not have child nodes.");
    }

// trigger testcase
// org/jsoup/nodes/TextNodeTest.java::testLeadNodesHaveNoChildren
@Test public void testLeadNodesHaveNoChildren() {
        Document doc = Jsoup.parse("<div>Hello there</div>");
        Element div = doc.select("div").first();
        TextNode tn = (TextNode) div.childNode(0);
        List<Node> nodes = tn.childNodes();
        assertEquals(0, nodes.size());
    }
