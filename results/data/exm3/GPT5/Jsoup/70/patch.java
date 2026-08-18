static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        int depth = 0;
        Node cur = node;
        while (cur != null && depth < 6) {
            if (cur instanceof Element) {
                Element el = (Element) cur;
                if (el.tag.preserveWhitespace())
                    return true;
            }
            cur = cur.parentNode();
            depth++;
        }
        return false;
    }