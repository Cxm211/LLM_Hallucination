// org/apache/commons/collections/TestExtendedProperties.java
public void testCollections299_overrideNonStringIgnoresDefaultString() {
        Properties defaults = new Properties();
        defaults.put("mixedKey", "stringDefault");

        Properties properties = new Properties(defaults);
        properties.put("mixedKey", Boolean.TRUE);

        ExtendedProperties extended = ExtendedProperties.convertProperties(properties);

        assertNull(extended.getString("mixedKey"));
        assertNull(extended.get("mixedKey"));
    }