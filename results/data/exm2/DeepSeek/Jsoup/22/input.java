    public Elements siblingElements() {

        return parent().children();
    }

    public Element nextElementSibling() {
        List<Element> siblings = parent().children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (siblings.size() > index+1)
            return siblings.get(index+1);
        else
            return null;
    }

    public Element previousElementSibling() {
        List<Element> siblings = parent().children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index-1);
        else
            return null;
    }

     include this node (a node is not a sibling of itself).
     @return node siblings. If the node has no parent, returns an empty list.
     */
    public List<Node> siblingNodes() {

        return parent().childNodes();
    }

    public Node previousSibling() {

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index-1);
        else
            return null;
    }

    public Elements() {
        contents = new ArrayList<Element>();
    }

// trigger testcase
@Test public void elementIsNotASiblingOfItself() {
        Document doc = Jsoup.parse("<div><p>One<p>Two<p>Three</div>");
        Element p2 = doc.select("p").get(1);

        assertEquals("Two", p2.text());
        Elements els = p2.siblingElements();
        assertEquals(2, els.size());
        assertEquals("<p>One</p>", els.get(0).outerHtml());
        assertEquals("<p>Three</p>", els.get(1).outerHtml());
    }

@Test public void nodeIsNotASiblingOfItself() {
        Document doc = Jsoup.parse("<div><p>One<p>Two<p>Three</div>");
        Element p2 = doc.select("p").get(1);

        assertEquals("Two", p2.text());
        List<Node> nodes = p2.siblingNodes();
        assertEquals(2, nodes.size());
        assertEquals("<p>One</p>", nodes.get(0).outerHtml());
        assertEquals("<p>Three</p>", nodes.get(1).outerHtml());
    }

@Test public void orphanNodeReturnsNullForSiblingElements() {
        Node node = new Element(Tag.valueOf("p"), "");
        Element el = new Element(Tag.valueOf("p"), "");

        assertEquals(0, node.siblingIndex());
        assertEquals(0, node.siblingNodes().size());

        assertNull(node.previousSibling());
        assertNull(node.nextSibling());

        assertEquals(0, el.siblingElements().size());
        assertNull(el.previousElementSibling());
        assertNull(el.nextElementSibling());
    }
