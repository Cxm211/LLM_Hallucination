    public FormElement addElement(Element element) {
        if (elements == null) elements = new ArrayList<>();
        elements.add(element);
        return this;
    }