public Boolean getBoolean(String key, Boolean defaultValue) {

        Object value = get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
            
        } else if (value instanceof String) {
            String s = testBoolean((String) value);
            Boolean b = new Boolean(s);
            put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getBoolean(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Boolean object");
        }
    }