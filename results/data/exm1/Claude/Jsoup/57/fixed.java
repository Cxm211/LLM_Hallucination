// ===== FIXED org.jsoup.nodes.Attributes :: removeIgnoreCase(String) [lines 118-127] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-57-fixed/src/main/java/org/jsoup/nodes/Attributes.java =====
    public void removeIgnoreCase(String key) {
        Validate.notEmpty(key);
        if (attributes == null)
            return;
        for (Iterator<String> it = attributes.keySet().iterator(); it.hasNext(); ) {
            String attrKey = it.next();
            if (attrKey.equalsIgnoreCase(key))
                it.remove();
        }
    }
