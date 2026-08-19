// ===== FIXED org.jsoup.nodes.Element :: equals(Object) [lines 1168-1176] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-41-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Element element = (Element) o;

        return tag.equals(element.tag);
    }
