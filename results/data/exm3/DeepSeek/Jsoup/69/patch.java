    public FormElement addElement(Element element) {
        elements.add(element);
        element.form(this);
        return this;
    }