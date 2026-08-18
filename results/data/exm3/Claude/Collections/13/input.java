// buggy function
    public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            c.setProperty(s, props.getProperty(s));
        }

        return c;
    }

// trigger testcase
// org/apache/commons/collections/TestExtendedProperties.java::testCollections299
public void testCollections299() {
        Properties defaults = new Properties();
        defaults.put("objectTrue", Boolean.TRUE);

        Properties properties = new Properties(defaults);
        properties.put("objectFalse", Boolean.FALSE);

        ExtendedProperties extended = ExtendedProperties.convertProperties(properties);

        assertNull(extended.getString("objectTrue"));
        assertNull(extended.getString("objectFalse"));

        assertNull(extended.get("objectTrue"));
        assertNull(extended.get("objectFalse"));
    }
