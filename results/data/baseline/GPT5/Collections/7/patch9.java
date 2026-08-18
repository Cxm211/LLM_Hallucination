public Integer getInteger(String key, Integer defaultValue) {
        Object value = get(key);

        if (value instanceof Integer) {
            return (Integer) value;
            
        } else if (value instanceof String) {
            Integer i = new Integer((String) value);
            put(key, i);
            return i;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getInteger(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Integer object");
        }
    }