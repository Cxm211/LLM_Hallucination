public FormElement addElement(Element element) {
    if (element.parent() == this) {
        elements.add(element);
    }
    return this;
}