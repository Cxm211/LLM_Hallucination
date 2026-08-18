public FormElement addElement(Element element) {
    if (element.parent() != null) {
        element.remove();
    }
    elements.add(element);
    return this;
}