// buggy code
    public XmlDeclaration asXmlDeclaration() {
        String data = getData();
        Document doc = Jsoup.parse("<" + data.substring(1, data.length() -1) + ">", baseUri(), Parser.xmlParser());
        XmlDeclaration decl = null;
        if (doc.childNodeSize() > 0) {
            Element el = doc.child(0);
            decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()), data.startsWith("!"));
            decl.attributes().addAll(el.attributes());
        }
        return decl;
    }

// relevant test
// org.jsoup.select.SelectorTest::selectFirstWithAnd
    @Test public void selectFirstWithAnd() {
        String html = "<p>One<p class=foo>Two<p>Three";
        Document doc = Jsoup.parse(html);
        assertEquals("Two", doc.selectFirst("p.foo").text());
    }

// org.jsoup.select.SelectorTest::selectFirstWithOr
    @Test public void selectFirstWithOr() {
        String html = "<p>One<p>Two<p>Three<div>Four";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.selectFirst("p, div").text());
    }

// org.jsoup.select.SelectorTest::matchText
    @Test public void matchText() {
        String html = "<p>One<br>Two</p>";
        Document doc = Jsoup.parse(html);
        String origHtml = doc.html();

        Elements one = doc.select("p:matchText:first-child");
        assertEquals("One", one.first().text());

        Elements two = doc.select("p:matchText:last-child");
        assertEquals("Two", two.first().text());

        assertEquals(origHtml, doc.html());

        assertEquals("Two", doc.select("p:matchText + br + *").text());
    }

// org.jsoup.select.SelectorTest::splitOnBr
    @Test public void splitOnBr() {
        String html = "<div><p>One<br>Two<br>Three</p></div>";
        Document doc = Jsoup.parse(html);

        Elements els = doc.select("p:matchText");
        assertEquals(3, els.size());
        assertEquals("One", els.get(0).text());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).toString());
    }

// org.jsoup.select.SelectorTest::matchTextAttributes
    @Test public void matchTextAttributes() {
        Document doc = Jsoup.parse("<div><p class=one>One<br>Two<p class=two>Three<br>Four");
        Elements els = doc.select("p.two:matchText:last-child");

        assertEquals(1, els.size());
        assertEquals("Four", els.text());
    }

// org.jsoup.select.SelectorTest::findBetweenSpan
    @Test public void findBetweenSpan() {
        Document doc = Jsoup.parse("<p><span>One</span> Two <span>Three</span>");
        Elements els = doc.select("span ~ p:matchText"); 

        assertEquals(1, els.size());
        assertEquals("Two", els.text());
    }

// org.jsoup.select.TraversorTest::filterVisit
    public void filterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipChildren
    public void filterSkipChildren() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipEntirely
    public void filterSkipEntirely() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div><div><#text></#text></div>", accum.toString());
    }

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
