public Element clone() {
    Element clone = (Element) super.clone();
    clone.classNames = new LinkedHashSet<String>(this.classNames);
    return clone;
}