protected List<Node> ensureChildNodes() {
    if (childNodes == null)
        childNodes = new NodeList(this, 4);
    return childNodes;
}