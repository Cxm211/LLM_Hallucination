public Double getDouble(String key, Double defaultValue) {
        Object value = get(key);

        if (value instanceof Double) {
            return (Double) value;
            
        } else if (value instanceof String) {
            Double d = new Double((String) value);
            put(key, d);
            return d;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getDouble(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Double object");
        }
    }