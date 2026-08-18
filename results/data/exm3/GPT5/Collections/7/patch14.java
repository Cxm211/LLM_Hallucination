public void putAll(Map map) {
        if (map instanceof ExtendedProperties) {
            for (Iterator it = ((ExtendedProperties) map).getKeys(); it.hasNext(); ) {
                Object key = it.next();
                addPropertyDirect(String.valueOf(key), map.get(key));
            }
        } else {
            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                addPropertyDirect(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
    }