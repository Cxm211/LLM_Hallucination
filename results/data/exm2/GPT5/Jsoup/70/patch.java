static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        if (node != null) {
            int i = 0;
            Node n = node;
            while (i < 6 && n != null) {
                if (n instanceof Element) {
                    Element el = (Element) n;
                    if (el.tag.preserveWhitespace())
                        return true;
                }
                n = n.parent();
                i++;
            }
        }
        return false;
    }