// org/apache/commons/collections/map/TestMultiValueMap.java
public void testPutNewKey() {
    MultiValueMap test = MultiValueMap.decorate(new HashMap(), ArrayList.class);
    assertEquals("x", test.put("NewKey", "x"));
    assertEquals(1, test.size());
    assertEquals(1, test.size("NewKey"));
    assertEquals(true, test.containsValue("NewKey", "x"));
}