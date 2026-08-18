// ===== FIXED org.apache.commons.collections.map.CaseInsensitiveMap :: convertKey(Object) [lines 119-129] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-14-fixed/src/java/org/apache/commons/collections/map/CaseInsensitiveMap.java =====
    protected Object convertKey(Object key) {
        if (key != null) {
            char[] chars = key.toString().toCharArray();
            for (int i = chars.length - 1; i >= 0; i--) {
                chars[i] = Character.toLowerCase(Character.toUpperCase(chars[i]));
            }
            return new String(chars);
        } else {
            return AbstractHashedMap.NULL;
        }
    }   
