static Map<Object, Object> getRegistry() {
    return REGISTRY.get() != null ? REGISTRY.get() : new HashMap<Object, Object>();
}