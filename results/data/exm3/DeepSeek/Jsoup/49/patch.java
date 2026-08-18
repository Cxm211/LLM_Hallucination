    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        int insertIdx = index;
        for (Node child : children) {
            reparentChild(child);
            if (insertIdx > childNodes.size()) {
                insertIdx = childNodes.size();
            }
            childNodes.add(insertIdx, child);
            insertIdx++;
        }
        reindexChildren(index);
    }