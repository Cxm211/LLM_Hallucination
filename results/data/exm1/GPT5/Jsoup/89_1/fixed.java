// ===== FIXED org.jsoup.nodes.Attribute :: setValue(String) [lines 87-97] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-89-fixed/src/main/java/org/jsoup/nodes/Attribute.java =====
    public String setValue(String val) {
        String oldVal = this.val;
        if (parent != null) {
            oldVal = parent.get(this.key); // trust the container more
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound)
                parent.vals[i] = val;
        }
        this.val = val;
        return Attributes.checkNotNull(oldVal);
    }
