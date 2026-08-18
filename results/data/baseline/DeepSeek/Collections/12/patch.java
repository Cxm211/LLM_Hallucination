    public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            if (!super.containsKey(key)) {
                super.put(key, props.get(key));
            }
        }
    }