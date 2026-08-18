protected boolean processEndTag(String name) {
    end.reset();
    end.name(name);
    return process(end);
}