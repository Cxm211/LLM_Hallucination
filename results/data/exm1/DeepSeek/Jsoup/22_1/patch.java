public Elements siblingElements() {
    Elements siblings = parent().children();
    Elements result = new Elements();
    for (Element sibling : siblings) {
        if (sibling != this)
            result.add(sibling);
    }
    return result;
}