// buggy code
    protected List<Node> ensureChildNodes() {
        throw new UnsupportedOperationException("Leaf Nodes do not have child nodes.");
    }

// relevant test
// org.jsoup.select.TraversorTest::filterRemove
    public void filterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                
                return ("b".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div>\n<div>\n There be \n</div>", doc.select("body").html());
    }

// org.jsoup.select.TraversorTest::filterStop
    public void filterStop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p>", accum.toString());
    }
