private String updateNamespaces(org.jsoup.nodes.Element el) {
    // determine this element's namespace URI by resolving xmlns declarations in scope
    String tagName = el.tagName();
    int pos = tagName.indexOf(":");
    String prefix = pos > 0 ? tagName.substring(0, pos) : "";
    // search current element and ancestors for matching xmlns declaration
    org.jsoup.nodes.Element cur = el;
    while (cur != null) {
        Attributes attrs = cur.attributes();
        for (Attribute attr : attrs) {
            String key = attr.getKey();
            if (prefix.isEmpty()) {
                if (key.equals(xmlnsKey))
                    return attr.getValue();
            } else {
                if (key.startsWith(xmlnsPrefix) && key.substring(xmlnsPrefix.length()).equals(prefix))
                    return attr.getValue();
            }
        }
        org.jsoup.nodes.Node parent = cur.parent();
        cur = parent instanceof org.jsoup.nodes.Element ? (org.jsoup.nodes.Element) parent : null;
    }
    return null;
}