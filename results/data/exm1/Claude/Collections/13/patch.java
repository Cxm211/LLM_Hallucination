public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        java.util.Set allKeys = new java.util.HashSet();
        
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            allKeys.add((String) e.nextElement());
        }
        
        Properties defaults = props;
        while ((defaults = ((Properties)defaults).defaults) != null) {
            for (Enumeration e = defaults.propertyNames(); e.hasMoreElements();) {
                allKeys.add((String) e.nextElement());
            }
        }
        
        for (java.util.Iterator it = allKeys.iterator(); it.hasNext();) {
            String s = (String) it.next();
            String value = props.getProperty(s);
            if (value != null) {
                c.setProperty(s, value);
            }
        }

        return c;
    }