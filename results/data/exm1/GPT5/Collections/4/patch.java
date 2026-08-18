public Object put(Object key, Object value) {
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(1);
            getMap().put(key, coll);
        }
        boolean result = coll.add(value);
        return (result ? value : null);
    }