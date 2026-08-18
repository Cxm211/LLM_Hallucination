// ===== FIXED org.apache.commons.collections.ExtendedProperties :: convertProperties(Properties) [lines 1719-1731] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-13-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            String value = props.getProperty(s);
            if(value != null) {
                c.setProperty(s, value);
            }
        }

        return c;
    }
