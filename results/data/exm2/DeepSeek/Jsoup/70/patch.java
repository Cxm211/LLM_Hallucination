static boolean preserveWhitespace(Node node) {
    // looks only at this element and five levels up, to prevent recursion & needless stack searches
    Element el = null;
    if (node instanceof Element) {
        el = (Element) node;
    } else if (node != null) {
        Node parent = node.parent();
        if (parent instanceof Element) {
            el = (Element) parent;
        }
    }
    for (int i = 0; i < 6 && el != null; i++) {
        if (el.tag.preserveWhitespace())
            return true;
        el = el.parent();
    }
    return false;
}