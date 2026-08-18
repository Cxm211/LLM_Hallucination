static Map<Object, Object> getRegistry() {
        Map<Object, Object> m = REGISTRY.get();
        if (m == null) {
            m = new java.util.HashMap<Object, Object>();
            REGISTRY.set(m);
        }
        return m;
    }