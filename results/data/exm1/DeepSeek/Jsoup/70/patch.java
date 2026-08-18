    static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        if (node == null) return false;
        if (!(node instanceof Element))
            node = node.parent();
        int level = 0;
        while (node instanceof Element && level < 5) {
            Element el = (Element) node;
            if (el.tag.preserveWhitespace())
                return true;
            node = el.parent();
            level++;
        }
        return false;
    }