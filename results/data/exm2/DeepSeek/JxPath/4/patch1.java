private Object getValueForDOM() {
    if (node instanceof org.w3c.dom.Node) {
        return stringValue((org.w3c.dom.Node) node);
    }
    return null;
}