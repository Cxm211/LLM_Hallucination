    public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            Vector values = props.getVector(key);
            for (int i = 0; i < values.size(); i++) {
                addProperty(key, values.elementAt(i));
            }
        }
    }