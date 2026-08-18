public void putAll(Map map) {
        if (map instanceof ExtendedProperties) {
            for (Iterator it = ((ExtendedProperties) map).getKeys(); it.hasNext(); ) {
                Object key = it.next();
                String skey = (String) key;
                addPropertyDirect(skey, map.get(key));
            }
        } else {
            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String skey = (String) entry.getKey();
                addPropertyDirect(skey, entry.getValue());
            }
        }
    }