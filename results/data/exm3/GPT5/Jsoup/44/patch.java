protected boolean processStartTag(String name) {
        // Ensure a fresh, empty Attributes so no attributes can leak from a previous token
        start.reset();
        start.nameAttr(name, new Attributes());
        return process(start);
    }