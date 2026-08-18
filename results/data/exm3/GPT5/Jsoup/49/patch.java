protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        ensureChildNodes();
        int currentIndex = index;
        for (int i = 0; i < children.length; i++) {
            Node in = children[i];
            if (in.parentNode == this) {
                int inIndex = in.siblingIndex();
                if (inIndex < currentIndex)
                    currentIndex--;
            }
            reparentChild(in);
            childNodes.add(currentIndex, in);
            currentIndex++;
        }
        reindexChildren(index);
    }