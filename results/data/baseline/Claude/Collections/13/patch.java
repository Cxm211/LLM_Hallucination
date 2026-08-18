public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        if (props != null) {
            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String s = (String) e.nextElement();
                c.setProperty(s, props.getProperty(s));
            }
        }

        return c;
    }