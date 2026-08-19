// ===== FIXED org.apache.commons.lang3.builder.ToStringStyle :: getRegistry() [lines 147-149] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-34-fixed/src/main/java/org/apache/commons/lang3/builder/ToStringStyle.java =====
    static Map<Object, Object> getRegistry() {
        return REGISTRY.get();
    }

// ===== FIXED org.apache.commons.lang3.builder.ToStringStyle :: isRegistered(Object) [lines 162-165] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-34-fixed/src/main/java/org/apache/commons/lang3/builder/ToStringStyle.java =====
    static boolean isRegistered(Object value) {
        Map<Object, Object> m = getRegistry();
        return m != null && m.containsKey(value);
    }
