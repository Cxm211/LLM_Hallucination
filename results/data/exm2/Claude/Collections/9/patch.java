public void combine(ExtendedProperties props) {
    for (Iterator it = props.getKeys(); it.hasNext();) {
        String key = (String) it.next();
        Object value = props.get(key);
        if (value instanceof String) {
            addProperty(key, value);
        } else {
            setProperty(key, value);
        }
    }
}