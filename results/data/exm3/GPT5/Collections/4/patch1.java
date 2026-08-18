public boolean putAll(Object key, Collection values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(values.size());  // might produce a non-empty collection
            boolean result = coll.addAll(values);
            // always put the created collection into the map
            getMap().put(key, coll);
            return result;
        } else {
            return coll.addAll(values);
        }
    }