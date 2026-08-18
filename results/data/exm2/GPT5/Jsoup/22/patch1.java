public Element nextElementSibling() {
    Element parent = parent();
    if (parent == null) return null;
    List<Element> siblings = parent.children();
    Integer index = indexInList(this, siblings);
    if (index != null && siblings.size() > index + 1)
        return siblings.get(index + 1);
    return null;
}