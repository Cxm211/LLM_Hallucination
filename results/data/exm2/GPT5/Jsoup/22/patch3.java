public List<Node> siblingNodes() {
    if (parentNode == null) return new ArrayList<Node>(0);
    List<Node> nodes = parentNode.childNodes();
    List<Node> out = new ArrayList<Node>(Math.max(0, nodes.size() - 1));
    for (Node n : nodes) {
        if (n != this) out.add(n);
    }
    return out;
}