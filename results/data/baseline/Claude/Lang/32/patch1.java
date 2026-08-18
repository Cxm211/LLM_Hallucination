static boolean isRegistered(Object value) {
    Set<IDKey> registry = getRegistry();
    if (registry == null) {
        return false;
    }
    return registry.contains(new IDKey(value));
}