// org/apache/commons/collections/map/TestCaseInsensitiveMap.java
public void testNullKeyHandling() {
    CaseInsensitiveMap map = new CaseInsensitiveMap();
    
    // Test null key behavior
    map.put(null, "null_value");
    assertEquals("Should retrieve value for null key", "null_value", map.get(null));
    
    // Test that null is treated consistently
    assertTrue("Map should contain null key", map.containsKey(null));
    
    // Overwrite null key
    map.put(null, "new_null_value");
    assertEquals("Should retrieve updated value for null key", "new_null_value", map.get(null));
    assertEquals("Map should have size 1 after overwriting null", 1, map.size());
}