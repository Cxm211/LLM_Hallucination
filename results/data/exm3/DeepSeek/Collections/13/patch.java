    public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            String val = props.getProperty(s);
            if (val != null) {
                c.setProperty(s, val);
            }
        }

        return c;
    }