public FormElement addElement(Element element) {
    elements.add(element);
    addChildren(element);
    return this;
}