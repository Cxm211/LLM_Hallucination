// ===== FIXED org.apache.commons.lang3.builder.HashCodeBuilder :: isRegistered(Object) [lines 146-149] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-32-fixed/src/main/java/org/apache/commons/lang3/builder/HashCodeBuilder.java =====
    static boolean isRegistered(Object value) {
        Set<IDKey> registry = getRegistry();
        return registry != null && registry.contains(new IDKey(value));
    }

// ===== FIXED org.apache.commons.lang3.builder.HashCodeBuilder :: unregister(Object) [lines 538-548] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-32-fixed/src/main/java/org/apache/commons/lang3/builder/HashCodeBuilder.java =====
    static void unregister(Object value) {
        Set<IDKey> s = getRegistry();
        if (s != null) {
            s.remove(new IDKey(value));
            synchronized (HashCodeBuilder.class) {
                if (s.isEmpty()) {
                    REGISTRY.remove();
                }
            }
        }
    }
