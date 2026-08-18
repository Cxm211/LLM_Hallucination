// org/apache/commons/collections/TestExtendedProperties.java
public void testConvertPropertiesMixed() {
        Properties props = new Properties();
        props.put("stringKey", "stringValue");
        props.put("integerKey", Integer.valueOf(42));
        ExtendedProperties extended = ExtendedProperties.convertProperties(props);
        // string key should be present
        assertTrue(extended.containsKey("stringKey"));
        assertEquals("stringValue", extended.getString("stringKey"));
        assertEquals("stringValue", extended.get("stringKey"));
        // non-string key should not be present
        assertFalse(extended.containsKey("integerKey"));
        assertNull(extended.getString("integerKey"));
        assertNull(extended.get("integerKey"));
    }
