// com/google/gson/functional/CollectionTest.java
public void testNestedGenericsThreeLevels() {
    String json = "{\"key\": [[1,2,3]]}";
    Type type = new TypeToken<Map<String, List<Set<Integer>>>>() {}.getType();
    Map<String, List<Set<Integer>>> map = new Gson().fromJson(json, type);
    assertNotNull(map);
    List<Set<Integer>> list = map.get(\"key\");
    assertNotNull(list);
    assertEquals(1, list.size());
    Set<Integer> set = list.get(0);
    assertNotNull(set);
    assertTrue(set.contains(1));
    assertTrue(set.contains(2));
    assertTrue(set.contains(3));
  }
