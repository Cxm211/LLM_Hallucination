public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();
        for (String s : props.stringPropertyNames()) {
            c.setProperty(s, props.getProperty(s));
        }
        return c;
    }