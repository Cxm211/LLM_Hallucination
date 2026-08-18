public Elements siblingElements() {
    if (parent() == null) return new Elements();
    List<Element> siblings = parent().children();
    Elements result = new Elements();
    for (Element e : siblings) {
        if (e != this) result.add(e);
    }
    return result;
}