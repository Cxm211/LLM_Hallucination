public Short getShort(String key, Short defaultValue) {
    Object value = get(key);

    if (value instanceof Short) {
        return (Short) value;
        
    } else if (value instanceof String) {
        Short s = new Short((String) value);
        put(key, s);
        return s;
        
    } else if (value == null) {
        if (defaults != null) {
            return defaults.getShort(key, defaultValue);
        } else {
            return defaultValue;
        }
    } else {
        throw new ClassCastException('\'' + key + "' doesn't map to a Short object");
    }
}