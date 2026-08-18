public void putAll(Map map) {
    if (map instanceof ExtendedProperties) {
        for (Iterator it = ((ExtendedProperties) map).getKeys(); it.hasNext(); ) {
            Object key = it.next();
            if (!containsKey(key)) {
                keysAsListed.add(key);
            }
            put(key, map.get(key));
        }
    } else {
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            if (!containsKey(key)) {
                keysAsListed.add(key);
            }
            put(entry.getKey(), entry.getValue());
        }
    }
}