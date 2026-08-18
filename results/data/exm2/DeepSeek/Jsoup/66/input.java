    protected List<Node> ensureChildNodes() {
        if (childNodes == EMPTY_NODES) {
            childNodes = new NodeList(4);
        }
        return childNodes;
    }

    protected Element doClone(Node parent) {
        Element clone = (Element) super.doClone(parent);
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new NodeList(childNodes.size());
        clone.childNodes.addAll(childNodes);

        return clone;
    }

        NodeList(int initialCapacity) {
            super(initialCapacity);
        }

        public void onContentsChanged() {
            nodelistChanged();
        }

// trigger testcase
@Test
    public void testNextElementSiblingAfterClone() {
        // via https://github.com/jhy/jsoup/issues/951
        String html = "<!DOCTYPE html><html lang=\"en\"><head></head><body><div>Initial element</div></body></html>";
        String expectedText = "New element";
        String cloneExpect = "New element in clone";

        Document original = Jsoup.parse(html);
        Document clone = original.clone();

        Element originalElement = original.body().child(0);
        originalElement.after("<div>" + expectedText + "</div>");
        Element originalNextElementSibling = originalElement.nextElementSibling();
        Element originalNextSibling = (Element) originalElement.nextSibling();
        assertEquals(expectedText, originalNextElementSibling.text());
        assertEquals(expectedText, originalNextSibling.text());

        Element cloneElement = clone.body().child(0);
        cloneElement.after("<div>" + cloneExpect + "</div>");
        Element cloneNextElementSibling = cloneElement.nextElementSibling();
        Element cloneNextSibling = (Element) cloneElement.nextSibling();
        assertEquals(cloneExpect, cloneNextElementSibling.text());
        assertEquals(cloneExpect, cloneNextSibling.text());
    }
