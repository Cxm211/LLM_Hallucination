// ===== FIXED org.jsoup.nodes.Element :: preserveWhitespace(Node) [lines 1087-1100] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-70-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        if (node != null && node instanceof Element) {
            Element el = (Element) node;
            int i = 0;
            do {
                if (el.tag.preserveWhitespace())
                    return true;
                el = el.parent();
                i++;
            } while (i < 6 && el != null);
        }
        return false;
    }
