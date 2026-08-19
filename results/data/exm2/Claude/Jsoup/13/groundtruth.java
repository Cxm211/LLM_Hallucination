// ===== FIXED org.jsoup.nodes.Node :: hasAttr(String) [lines 104-113] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-13-fixed/src/main/java/org/jsoup/nodes/Node.java =====
    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.toLowerCase().startsWith("abs:")) {
            String key = attributeKey.substring("abs:".length());
            if (attributes.hasKey(key) && !absUrl(key).equals(""))
                return true;
        }
        return attributes.hasKey(attributeKey);
    }
