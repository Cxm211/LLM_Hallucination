static boolean preserveWhitespace(Node node) {
    if (node != null && node instanceof Element) {
        Element el = (Element) node;
        int count = 0;
        while (el != null && count < 5) {
            if (el.tag.preserveWhitespace())
                return true;
            el = el.parent();
            count++;
        }
        return false;
    }
    return false;
}