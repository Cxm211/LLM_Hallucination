public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            this.setProperty(key, props.getProperty(key));
        }
    }