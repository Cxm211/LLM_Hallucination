private void normaliseTextNodes(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        List<Node> toMoveFromNoscript = new ArrayList<Node>();
        if (element.tagName().equals("head")) {
            for (Node node : element.childNodes) {
                if (node instanceof Element) {
                    Element el = (Element) node;
                    if (el.tagName().equals("noscript")) {
                        for (Node cn : el.childNodes) {
                            if (cn instanceof Element) {
                                toMoveFromNoscript.add(cn);
                            }
                        }
                    }
                }
            }
        }

        for (int i = toMove.size()-1; i >= 0; i--) {
            Node node = toMove.get(i);
            element.removeChild(node);
            body().prependChild(new TextNode(" ", ""));
            body().prependChild(node);
        }

        for (int i = toMoveFromNoscript.size()-1; i >= 0; i--) {
            Node node = toMoveFromNoscript.get(i);
            ((Element) node.parent()).removeChild(node);
            body().prependChild(new TextNode(" ", ""));
            body().prependChild(node);
        }
    }