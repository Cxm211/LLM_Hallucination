    public List getList(String key, List defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new ArrayList((List) value);
            
        } else if (value instanceof String) {
            List values = new ArrayList(1);
            values.add(value);
            put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getList(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new ArrayList() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a List object");
        }
    }