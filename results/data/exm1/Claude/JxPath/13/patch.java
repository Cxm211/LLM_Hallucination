public synchronized String getNamespaceURI(String prefix) {
    String uri = (String) namespaceMap.get(prefix);
    if (uri == null && pointer != null) {
        uri = pointer.getNamespaceURI(prefix);
    }
    if (uri == null && parent != null) {
        return parent.getNamespaceURI(prefix);
    }
    return uri;
}