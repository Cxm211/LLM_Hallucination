static Map<Object, Object> getRegistry() {
        Map<Object, Object> m = REGISTRY.get();
        return m != null ? m : Collections.<Object, Object>emptyMap();
    }