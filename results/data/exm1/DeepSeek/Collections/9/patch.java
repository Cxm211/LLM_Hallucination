public void combine(ExtendedProperties props) {
    for (Iterator it = props.getKeys(); it.hasNext();) {
        String key = (String) it.next();
        Object value = props.get(key);
        if (value instanceof Vector) {
            Vector v = (Vector) value;
            for (int i = 0; i < v.size(); i++) {
                addProperty(key, v.get(i));
            }
        } else {
            setProperty(key, value);
        }
    }
}