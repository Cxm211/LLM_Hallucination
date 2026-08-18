protected Set<IDKey> initialValue() {
    // Ensure that calling get() on the ThreadLocal does not create a Set by default.
    // This allows getRegistry() to return null when no registry is in use.
    return null;
}