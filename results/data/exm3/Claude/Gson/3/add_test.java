// com/google/gson/functional/MapTest.java
public void testNavigableMap() throws Exception {
    Type typeOfMap = new TypeToken<NavigableMap<Integer, String>>() {}.getType();
    NavigableMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }