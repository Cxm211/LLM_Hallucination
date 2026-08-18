// com/google/gson/functional/MapTest.java
public void testRawConcurrentMap() throws Exception {
    ConcurrentMap map = gson.fromJson("{\"123\":\"456\"}", ConcurrentMap.class);
    assertEquals(1, map.size());
    assertTrue(map.containsKey("123"));
    assertEquals("456", map.get("123"));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }
