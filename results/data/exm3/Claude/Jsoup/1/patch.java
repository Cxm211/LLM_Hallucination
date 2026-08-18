private void normalise(Element element) {
    List<Node> toMove = new ArrayList<Node>();
    for (Node node: element.childNodes) {
        if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            if (!tn.isBlank())
                toMove.add(tn);
        }
    }

    for (int i = 0; i < toMove.size(); i++) {
        Node node = toMove.get(i);
        element.removeChild(node);
        if (i > 0)
            body().appendChild(new TextNode(" ", ""));
        body().appendChild(node);
    }
}