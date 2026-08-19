// ===== FIXED org.apache.commons.collections.ExtendedProperties :: combine(ExtendedProperties) [lines 813-818] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-9-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            super.put(key, props.get(key));
        }
    }
