public static ExtendedProperties convertProperties(Properties props) {
    ExtendedProperties c = new ExtendedProperties();

    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
        String s = (String) e.nextElement();
        Object value = props.get(s);
        if (value instanceof String) {
            c.setProperty(s, (String) value);
        }
    }

    return c;
}