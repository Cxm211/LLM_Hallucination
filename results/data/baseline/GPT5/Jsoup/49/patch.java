protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        int startIndex = index;
        for (int i = 0; i < children.length; i++) {
            Node in = children[i];
            reparentChild(in);
            int insert = Math.min(index, childNodes.size());
            childNodes.add(insert, in);
            index = insert + 1;
        }
        reindexChildren(startIndex);
    }