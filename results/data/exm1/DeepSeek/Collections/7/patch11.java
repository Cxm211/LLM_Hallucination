public Float getFloat(String key, Float defaultValue) {
    Object value = get(key);
    if (value instanceof Float) {
        return (Float) value;
    } else if (value instanceof String) {
        Float f = new Float((String) value);
        put(key, f);
        return f;
    } else if (value == null) {
        if (defaults != null) {
            return defaults.getFloat(key, defaultValue);
        } else {
            return defaultValue;
        }
    } else {
        throw new ClassCastException('\'' + key + "' doesn't map to a Float object");
    }
}