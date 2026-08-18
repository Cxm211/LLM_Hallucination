public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            String v = props.getProperty(s);
            if (v != null) {
                c.setProperty(s, v);
            }
        }

        return c;
    }