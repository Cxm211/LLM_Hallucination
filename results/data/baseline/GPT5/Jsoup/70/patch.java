static boolean preserveWhitespace(Node node) {
        int depth = 0;
        while (node != null && depth < 6) { // this element and up to five levels of parents
            if (node instanceof Element) {
                Element el = (Element) node;
                if (el.tag.preserveWhitespace())
                    return true;
            }
            node = node.parent();
            depth++;
        }
        return false;
    }