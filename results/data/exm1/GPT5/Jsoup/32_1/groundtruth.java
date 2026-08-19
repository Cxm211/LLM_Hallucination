// ===== FIXED org.jsoup.nodes.Element :: clone() [lines 1136-1140] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-32-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public Element clone() {
        Element clone = (Element) super.clone();
        clone.classNames = null; // derived on first hit, otherwise gets a pointer to source classnames
        return clone;
    }
