static void register(Object value) {
    Set<IDKey> registry = getRegistry();
    if (registry != null) {
        registry.add(new IDKey(value));
    }
}