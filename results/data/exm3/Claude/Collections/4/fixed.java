// ===== FIXED org.apache.commons.collections.map.MultiValueMap :: put(Object, Object) [lines 204-219] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-4-fixed/src/java/org/apache/commons/collections/map/MultiValueMap.java =====
    public Object put(Object key, Object value) {
        boolean result = false;
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(1);  // might produce a non-empty collection
            coll.add(value);
            if (coll.size() > 0) {
                // only add if non-zero size to maintain class state
                getMap().put(key, coll);
                result = true;  // map definitely changed
            }
        } else {
            result = coll.add(value);
        }
        return (result ? value : null);
    }

// ===== FIXED org.apache.commons.collections.map.MultiValueMap :: putAll(Object, Collection) [lines 306-324] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-4-fixed/src/java/org/apache/commons/collections/map/MultiValueMap.java =====
    public boolean putAll(Object key, Collection values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        boolean result = false;
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(values.size());  // might produce a non-empty collection
            coll.addAll(values);
            if (coll.size() > 0) {
                // only add if non-zero size to maintain class state
                getMap().put(key, coll);
                result = true;  // map definitely changed
            }
        } else {
            result = coll.addAll(values);
        }
        return result;
    }
