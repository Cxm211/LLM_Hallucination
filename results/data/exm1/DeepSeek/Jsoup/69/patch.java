public FormElement addElement(Element element) {
        elements.add(element);
        if (element.tag().isFormSubmittable()) {
            element.attr("form", id());
        }
        return this;
    }