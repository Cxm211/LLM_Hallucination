public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            // Use processed value to ensure escape/interpolation semantics are preserved
            setProperty(key, props.getProperty(key));
        }
    }