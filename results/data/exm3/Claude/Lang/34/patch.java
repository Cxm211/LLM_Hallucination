static Map<Object, Object> getRegistry() {
        Map<Object, Object> registry = REGISTRY.get();
        if (registry == null) {
            registry = new HashMap<Object, Object>();
            REGISTRY.set(registry);
        }
        return registry;
    }