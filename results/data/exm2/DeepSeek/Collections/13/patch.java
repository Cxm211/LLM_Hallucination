    public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            String value = props.getProperty(s);
            if (value != null) {
                c.setProperty(s, value);
            }
        }

        return c;
    }