public Byte getByte(String key, Byte defaultValue) {
        Object value = get(key);

        if (value instanceof Byte) {
            return (Byte) value;
            
        } else if (value instanceof String) {
            Byte b = new Byte((String) value);
            put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getByte(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Byte object");
        }
    }