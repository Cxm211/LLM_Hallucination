static Map<Object, Object> getRegistry() {
        Map<Object, Object> registry = REGISTRY.get();
        return registry != null ? registry : Collections.<Object, Object>emptyMap();
    }