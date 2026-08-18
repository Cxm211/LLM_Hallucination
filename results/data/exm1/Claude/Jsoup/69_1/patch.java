public FormElement addElement(Element element) {
        if (element == null) return this;
        elements.add(element);
        return this;
    }