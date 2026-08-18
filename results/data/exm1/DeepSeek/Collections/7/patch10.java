public Long getLong(String key, Long defaultValue) {
    Object value = get(key);
    if (value instanceof Long) {
        return (Long) value;
    } else if (value instanceof String) {
        Long l = new Long((String) value);
        put(key, l);
        return l;
    } else if (value == null) {
        if (defaults != null) {
            return defaults.getLong(key, defaultValue);
        } else {
            return defaultValue;
        }
    } else {
        throw new ClassCastException('\'' + key + "' doesn't map to a Long object");
    }
}