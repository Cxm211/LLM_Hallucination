protected Set<IDKey> initialValue() {
    return new HashSet<IDKey>();
}

static boolean isRegistered(Object value) {
    Set<IDKey> registry = getRegistry();
    return registry != null && registry.contains(new IDKey(value));
}

static void register(Object value) {
    getRegistry().add(new IDKey(value));
}

static void unregister(Object value) {
    Set<IDKey> registry = getRegistry();
    if (registry != null) {
        registry.remove(new IDKey(value));
        if (registry.isEmpty()) {
            getRegistry().remove();
        }
    }
}