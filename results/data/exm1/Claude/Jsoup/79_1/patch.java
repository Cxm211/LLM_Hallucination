protected List<Node> ensureChildNodes() {
    if (childNodes == null) {
        childNodes = new ArrayList<Node>(4);
    }
    return childNodes;
}