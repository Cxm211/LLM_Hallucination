public FormElement addElement(Element element) {
        // Ensure element knows its owning form so it can update state on removal
        if (element != null) {
            element.setForm(this);
            elements.add(element);
        }
        return this;
    }