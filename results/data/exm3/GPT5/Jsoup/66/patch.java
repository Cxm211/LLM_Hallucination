protected List<Node> ensureChildNodes() {
        if (childNodes == null || childNodes == EMPTY_NODES) {
            childNodes = new NodeList(4);
        }
        return childNodes;
    }