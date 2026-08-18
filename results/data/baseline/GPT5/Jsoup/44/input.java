// buggy code
    protected boolean processStartTag(String name) {
        return process(start.reset().name(name));
    }

    public boolean processStartTag(String name, Attributes attrs) {
        start.reset();
        start.nameAttr(name, attrs);
        return process(start);
    }

    protected boolean processEndTag(String name) {
        return process(end.reset().name(name));
    }

