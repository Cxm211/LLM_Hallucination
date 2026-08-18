    static void register(Object value) {
        if (value != null) {
            getRegistry().add(new IDKey(value));
        }
    }
