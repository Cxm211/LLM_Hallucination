private boolean isSameFormattingElement(Element a, Element b) {
        return a.nodeName().equals(b.nodeName()) &&
                a.attributes().equals(b.attributes());
    }