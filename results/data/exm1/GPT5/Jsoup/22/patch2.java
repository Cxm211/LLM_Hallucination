public Element previousElementSibling() {
        Element parent = parent();
        if (parent == null)
            return null;
        List<Element> siblings = parent.children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index - 1);
        else
            return null;
    }