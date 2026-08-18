public void combine(ExtendedProperties props) {
    for (Iterator it = props.getKeys(); it.hasNext();) {
        String key = (String) it.next();
        Object value = props.get(key);
        if (value instanceof List) {
            for (Iterator valueIt = ((List) value).iterator(); valueIt.hasNext();) {
                super.addProperty(key, valueIt.next());
            }
        } else {
            super.addProperty(key, value);
        }
    }
}