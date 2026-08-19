// ===== FIXED org.jsoup.nodes.Element :: html() [lines 1098-1102] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-37-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public String html() {
        StringBuilder accum = new StringBuilder();
        html(accum);
        return getOutputSettings().prettyPrint() ? accum.toString().trim() : accum.toString();
    }
