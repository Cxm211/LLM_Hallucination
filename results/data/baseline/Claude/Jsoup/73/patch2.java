public void tail(org.jsoup.nodes.Node source, int depth) {
    if (source instanceof org.jsoup.nodes.Element && dest.getParentNode() != null) {
        org.w3c.dom.Node parent = dest.getParentNode();
        if (parent instanceof Element) {
            dest = (Element) parent; // undescend. cromulent.
        } else {
            dest = null;
        }
    }
}