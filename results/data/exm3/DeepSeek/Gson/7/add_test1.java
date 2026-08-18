// com/google/gson/functional/MapTest.java
public void testMapDeserializationWithLargeLongKey() {
    long longKey = 9007199254740993L;
    String json = String.format("{%d:\"value\"}", longKey);
    Type typeOfMap = new TypeToken<Map<Long, String>>() {}.getType();
    Map<Long, String> map = gson.fromJson(json, typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(longKey));
    assertEquals("value", map.get(longKey));
  }
