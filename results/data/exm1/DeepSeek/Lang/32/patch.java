        protected Set<IDKey> initialValue() {
            return null;
        }

    static boolean isRegistered(Object value) {
        Set<IDKey> registry = getRegistry();
        if (registry == null) {
            return false;
        }
        return registry.contains(new IDKey(value));
    }

    static void register(Object value) {
        Set<IDKey> registry = getRegistry();
        if (registry != null) {
            registry.add(new IDKey(value));
        }
    }

    static void unregister(Object value) {
        Set<IDKey> registry = getRegistry();
        if (registry != null) {
            registry.remove(new IDKey(value));
        }
    }