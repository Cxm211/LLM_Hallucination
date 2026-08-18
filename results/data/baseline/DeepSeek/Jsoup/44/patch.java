protected boolean processStartTag(String name) {
    start.reset();
    start.name(name);
    return process(start);
}