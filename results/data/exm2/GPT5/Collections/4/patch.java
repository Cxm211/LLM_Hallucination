public Object put(Object key, Object value) {
        boolean result = false;
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(1);  // might produce a non-empty collection
            result = coll.add(value);
            if (coll.size() > 0) {
                // only add if non-zero size to maintain class state
                getMap().put(key, coll);
            }
        } else {
            result = coll.add(value);
        }
        return (result ? value : null);
    }