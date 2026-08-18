public FormElement addElement(Element element) {
        if (element == null) return this;
        if (!elements.contains(element)) {
            elements.add(element);
        }
        return this;
    }