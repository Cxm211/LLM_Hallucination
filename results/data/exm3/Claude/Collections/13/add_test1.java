// org/apache/commons/collections/TestExtendedProperties.java
public void testCollections299_AdditionalCase2() {
    Properties properties = new Properties();
    properties.put("mixedKey1", "stringValue");
    properties.put("mixedKey2", new Double(3.14));
    properties.setProperty("mixedKey3", "anotherString");

    ExtendedProperties extended = ExtendedProperties.convertProperties(properties);

    assertEquals("stringValue", extended.getString("mixedKey1"));
    assertNull(extended.getString("mixedKey2"));
    assertEquals("anotherString", extended.getString("mixedKey3"));
}