// com/google/gson/functional/MapTest.java
public void testConcurrentMapWithStringKeys() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentMap<String, String>>() {}.getType();
    ConcurrentMap<String, String> map = gson.fromJson("{\"foo\":\"bar\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey("foo"));
    assertEquals("bar", map.get("foo"));
    String json = gson.toJson(map);
    assertEquals("{\"foo\":\"bar\"}", json);
  }
