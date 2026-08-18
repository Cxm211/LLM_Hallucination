public Element nextElementSibling() {
        Element parent = parent();
        if (parent == null)
            return null;
        List<Element> siblings = parent.children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (siblings.size() > index + 1)
            return siblings.get(index + 1);
        else
            return null;
    }