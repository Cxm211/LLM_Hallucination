// org/apache/commons/collections/TestExtendedProperties.java
public void testCollections299_AdditionalCase1() {
    Properties defaults = new Properties();
    defaults.put("stringDefault", "defaultValue");
    defaults.put("objectDefault", new Integer(42));

    Properties properties = new Properties(defaults);
    properties.put("stringProperty", "propertyValue");
    properties.put("objectProperty", new Long(99));

    ExtendedProperties extended = ExtendedProperties.convertProperties(properties);

    assertEquals("propertyValue", extended.getString("stringProperty"));
    assertNull(extended.getString("objectProperty"));
    assertEquals("defaultValue", extended.getString("stringDefault"));
    assertNull(extended.getString("objectDefault"));
}