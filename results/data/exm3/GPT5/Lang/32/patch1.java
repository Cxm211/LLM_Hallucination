static boolean isRegistered(Object value) {
    final Set<IDKey> registry = getRegistry();
    return registry != null && registry.contains(new IDKey(value));
}