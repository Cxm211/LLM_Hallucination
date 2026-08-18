    static void register(Object value) {
        Set<IDKey> registry = getRegistry();
        if (registry == null) {
            registry = new HashSet<IDKey>();
            REGISTRY.set(registry);
        }
        registry.add(new IDKey(value));
    }