public boolean putAll(Object key, Collection values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(values.size());
            getMap().put(key, coll);
        }
        return coll.addAll(values);
    }