public FormElement addElement(Element element) {
    if (element.parent() == null) {
        element.parentNode = this;
    }
    elements.add(element);
    return this;
}