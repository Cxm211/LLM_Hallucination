public FormElement addElement(Element element) {
    // Remove any elements that are no longer attached to the DOM
    elements.removeIf(el -> el.parent() == null);
    if (element != null) {
        elements.add(element);
    }
    return this;
}