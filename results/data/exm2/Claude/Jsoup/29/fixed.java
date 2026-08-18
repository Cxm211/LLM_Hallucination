// ===== FIXED org.jsoup.nodes.Document :: title() [lines 67-71] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-29-fixed/src/main/java/org/jsoup/nodes/Document.java =====
    public String title() {
        // title is a preserve whitespace tag (for document output), but normalised here
        Element titleEl = getElementsByTag("title").first();
        return titleEl != null ? StringUtil.normaliseWhitespace(titleEl.text()).trim() : "";
    }
