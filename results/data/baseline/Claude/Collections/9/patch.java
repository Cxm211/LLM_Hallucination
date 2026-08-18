public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            Object value = props.get(key);
            Object existingValue = get(key);
            if (existingValue == null) {
                setProperty(key, value);
            } else if (existingValue instanceof List) {
                ((List) existingValue).add(value);
            } else {
                List list = new java.util.ArrayList();
                list.add(existingValue);
                list.add(value);
                setProperty(key, list);
            }
        }
    }