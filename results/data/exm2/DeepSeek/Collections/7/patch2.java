    private void addPropertyInternal(String key, Object value) {
        Object current = this.get(key);

        if (current instanceof String) {
            // one object already in map - convert it to a vector
            List values = new Vector(2);
            values.add(current);
            values.add(value);
            put(key, values);
            
        } else if (current instanceof List) {
            // already a list - just add the new token
            ((List) current).add(value);
            
        } else {
            // brand new key - store in keysAsListed to retain order
            put(key, value);
        }
    }