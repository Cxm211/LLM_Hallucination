static void unregister(Object value) {
    Set<IDKey> registry = getRegistry();
    if (registry != null) {
        registry.remove(new IDKey(value));
        if (registry.isEmpty()) {
            getRegistry().remove();
        }
    }
}