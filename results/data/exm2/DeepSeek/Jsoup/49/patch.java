protected void addChildren(int index, Node... children) {
    Validate.noNullElements(children);
    ensureChildNodes();
    // First, remove any children that are already in childNodes
    int countBefore = 0;
    for (Node child : children) {
        int pos = childNodes.indexOf(child);
        if (pos != -1) {
            childNodes.remove(pos);
            if (pos < index) {
                countBefore++;
            }
        }
    }
    int insertIndex = index - countBefore;
    for (int i = 0; i < children.length; i++) {
        Node in = children[i];
        reparentChild(in);
        childNodes.add(insertIndex + i, in);
    }
    reindexChildren(insertIndex);
}