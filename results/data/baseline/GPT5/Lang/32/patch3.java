    static void unregister(Object value) {
        if (value != null) {
            getRegistry().remove(new IDKey(value));
        }
    }
