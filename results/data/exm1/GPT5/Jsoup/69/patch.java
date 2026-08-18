public FormElement addElement(Element element) {
        if (element == null)
            return this;
        // avoid duplicates
        if (elements.contains(element))
            return this;
        // only add if this form is actually an ancestor of the element
        Element parent = element.parent();
        boolean inThisForm = false;
        while (parent != null) {
            if (parent == this) {
                inThisForm = true;
                break;
            }
            parent = parent.parent();
        }
        if (!inThisForm)
            return this;
        elements.add(element);
        return this;
    }