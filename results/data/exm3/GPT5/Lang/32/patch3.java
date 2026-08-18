static void unregister(Object value) {
    final Set<IDKey> registry = getRegistry();
    if (registry != null) {
        registry.remove(new IDKey(value));
        if (registry.isEmpty()) {
            REGISTRY.remove();
        }
    }
}