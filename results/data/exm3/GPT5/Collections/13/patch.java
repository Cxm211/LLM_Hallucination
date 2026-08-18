public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            Object v = props.get(s);
            if (v instanceof String) {
                c.setProperty(s, (String) v);
            }
        }

        return c;
    }