public Object put(Object key, Object value) {
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(1);  // might produce a non-empty collection
            boolean added = coll.add(value);
            // always put the created collection into the map
            getMap().put(key, coll);
            return added ? value : null;
        } else {
            boolean added = coll.add(value);
            return added ? value : null;
        }
    }