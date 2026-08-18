protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            boolean sameParent = (in.parentNode == this);
            int oldIndex = sameParent ? in.siblingIndex() : -1;
            reparentChild(in);
            if (sameParent && oldIndex < index) {
                index--;
            }
            childNodes.add(index, in);
        }
        reindexChildren(index);
    }