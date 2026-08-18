static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        int depth = 0;
        while (node != null && depth < 6) {
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