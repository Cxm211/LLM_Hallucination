private void normalise(Element element) {
        // Only move stray non-blank text nodes from head into body
        if (!"head".equals(element.tagName()))
            return;

        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        for (Node node: toMove) {
            element.removeChild(node);
            body().appendChild(node);
        }
    }