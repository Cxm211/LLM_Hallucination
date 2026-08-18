public Vector getVector(String key, Vector defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new Vector((List) value);
            
        } else if (value instanceof String) {
            Vector values = new Vector(1);
            values.add(value);
            put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getVector(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new Vector() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Vector object");
        }
    }