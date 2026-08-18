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

private void normaliseStructure(String tag, Element htmlEl) {
    Elements elements = this.getElementsByTag(tag);
    Element master = elements.first();
    if (elements.size() > 1) {
        List<Node> toMove = new ArrayList<Node>();
        for (int i = 1; i < elements.size(); i++) {
            Node dupe = elements.get(i);
            for (Node node : dupe.childNodes)
                toMove.add(node);
            dupe.remove();
        }

        for (Node node : toMove) {
            master.appendChild(node);
        }
    }
}