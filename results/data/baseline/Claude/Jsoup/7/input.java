// buggy code
    public Document normalise() {
        Element htmlEl = findFirstElementByTagName("html", this);
        if (htmlEl == null)
            htmlEl = appendElement("html");
        if (head() == null)
            htmlEl.prependElement("head");
        if (body() == null)
            htmlEl.appendElement("body");

        // pull text nodes out of root, html, and head els, and push into body. non-text nodes are already taken care
        // of. do in inverse order to maintain text order.
        normaliseTextNodes(head());
        normaliseTextNodes(htmlEl);
        normaliseTextNodes(this);

        
        return this;
    }

    private void normaliseTextNodes(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        for (int i = toMove.size()-1; i >= 0; i--) {
            Node node = toMove.get(i);
            element.removeChild(node);
            body().prependChild(new TextNode(" ", ""));
            body().prependChild(node);
        }
    }

