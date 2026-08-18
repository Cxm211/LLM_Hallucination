public void combine(ExtendedProperties props) {
    for (Iterator it = props.getKeys(); it.hasNext();) {
        String key = (String) it.next();
        Object value = props.get(key);
        if (value instanceof Vector) {
            Vector vec = (Vector) value;
            for (int i = 0; i < vec.size(); i++) {
                addProperty(key, vec.get(i));
            }
        } else {
            addProperty(key, value);
        }
    }
}