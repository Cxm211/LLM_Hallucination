public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            Object value = props.get(key);
            this.put(key, value);
        }
    }