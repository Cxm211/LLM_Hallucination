public Elements siblingElements() {
    List<Element> siblings = parent().children();
    Elements result = new Elements();
    for (Element sibling : siblings) {
        if (sibling != this) {
            result.contents.add(sibling);
        }
    }
    return result;
}