    public Elements siblingElements() {
        Element parent = parent();
        if (parent == null) return new Elements();
        List<Element> siblings = parent.children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        Elements result = new Elements();
        for (int i = 0; i < siblings.size(); i++) {
            if (i != index) {
                result.add(siblings.get(i));
            }
        }
        return result;
    }