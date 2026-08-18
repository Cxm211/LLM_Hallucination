public Elements siblingElements() {
    Element parent = parent();
    if (parent == null) return new Elements();
    List<Element> siblings = parent.children();
    Elements els = new Elements();
    for (Element el : siblings) {
        if (el != this) els.add(el);
    }
    return els;
}