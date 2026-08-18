public List<Node> siblingNodes() {
    if (parent() == null)
        return Collections.emptyList();
    List<Node> nodes = parent().childNodes();
    List<Node> siblings = new ArrayList<Node>(nodes.size() - 1);
    for (Node node : nodes) {
        if (node != this)
            siblings.add(node);
    }
    return siblings;
}