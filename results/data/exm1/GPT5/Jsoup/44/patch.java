protected boolean processStartTag(String name) {
        // Ensure attributes are initialized to avoid null access in downstream processing
        start.reset();
        start.nameAttr(name, new Attributes());
        return process(start);
    }