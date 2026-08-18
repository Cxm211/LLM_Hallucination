static void unregister(Object value) {
    Set<IDKey> registry = getRegistry();
    registry.remove(new IDKey(value));
    if (registry.isEmpty()) {
        getRegistry().remove();
    }
}