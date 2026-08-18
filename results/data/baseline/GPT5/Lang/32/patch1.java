    static boolean isRegistered(Object value) {
        if (value == null) {
            return false;
        }
        return getRegistry().contains(new IDKey(value));
    }
