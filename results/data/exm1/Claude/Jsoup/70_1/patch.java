static boolean preserveWhitespace(Node node) {
    if (node != null && node instanceof Element) {
        Element el = (Element) node;
        int levels = 0;
        while (levels < 6 && el != null) {
            if (el.tag.preserveWhitespace())
                return true;
            el = el.parent();
            levels++;
        }
    }
    return false;
}