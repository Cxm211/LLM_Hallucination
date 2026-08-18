static boolean preserveWhitespace(Node node) {
    if (node != null && node instanceof Element) {
        Element el = (Element) node;
        int i = 0;
        while (i < 6 && el != null) {
            if (el.tag.preserveWhitespace())
                return true;
            el = el.parent();
            i++;
        }
    }
    return false;
}