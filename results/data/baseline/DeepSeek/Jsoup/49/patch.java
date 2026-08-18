    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        for (int i = 0; i < children.length; i++) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index + i, in);
        }
        reindexChildren(index);
    }