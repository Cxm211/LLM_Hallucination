static boolean isRegistered(Object value) {
    if (value == null) {
        return false;
    }
    Map<Object, Object> m = getRegistry();
    return m.containsKey(value);
}