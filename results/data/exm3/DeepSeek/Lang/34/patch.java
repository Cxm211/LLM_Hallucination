    static Map<Object, Object> getRegistry() {
        Map<Object, Object> map = REGISTRY.get();
        if (map == null) {
            map = new HashMap<Object, Object>();
            REGISTRY.set(map);
        }
        return map;
    }