public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            Object value = props.get(key);
            if (value instanceof java.util.Collection) {
                for (Iterator it2 = ((java.util.Collection) value).iterator(); it2.hasNext();) {
                    addProperty(key, it2.next());
                }
            } else {
                addProperty(key, value);
            }
        }
    }