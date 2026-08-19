// ===== FIXED org.jsoup.nodes.Element :: doClone(Node) [lines 1398-1406] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-66-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    protected Element doClone(Node parent) {
        Element clone = (Element) super.doClone(parent);
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new NodeList(clone, childNodes.size());
        clone.childNodes.addAll(childNodes);

        return clone;
    }

// ===== FIXED org.jsoup.nodes.Element :: ensureChildNodes() [lines 87-92] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-66-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    protected List<Node> ensureChildNodes() {
        if (childNodes == EMPTY_NODES) {
            childNodes = new NodeList(this, 4);
        }
        return childNodes;
    }
