// ===== FIXED org.jsoup.nodes.Attribute :: getValue() [lines 79-81] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-88-fixed/src/main/java/org/jsoup/nodes/Attribute.java =====
    public String getValue() {
        return Attributes.checkNotNull(val);
    }
