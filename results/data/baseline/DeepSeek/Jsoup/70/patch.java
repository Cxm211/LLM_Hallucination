static boolean preserveWhitespace(Node node) {
    // looks only at this element and five levels up, to prevent recursion & needless stack searches
    if (node == null || !(node instanceof Element)) {
        return false;
    }
    Element el = (Element) node;
    int count = 0;
    while (el != null && count < 6) { // check current element and up to 5 ancestors
        if (el.tag.preserveWhitespace()) {
            return true;
        }
        Node parent = el.parent();
        if (parent instanceof Element) {
            el = (Element) parent;
        } else {
            break;
        }
        count++;
    }
    return false;
}