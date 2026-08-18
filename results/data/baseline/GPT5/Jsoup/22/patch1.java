public Element nextElementSibling() {
        Element p = parent();
        if (p == null)
            return null;
        List<Element> siblings = p.children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (siblings.size() > index + 1)
            return siblings.get(index + 1);
        else
            return null;
    }