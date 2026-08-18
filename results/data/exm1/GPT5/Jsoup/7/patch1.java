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
            Element b = body();
            if (b.childNodeSize() > 0)
                b.prependChild(new TextNode(" ", ""));
            b.prependChild(node);
        }
    }