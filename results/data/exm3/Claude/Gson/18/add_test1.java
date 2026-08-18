// com/google/gson/functional/CollectionTest.java
public void testWildcardTypeWithMultipleBounds() {
  String json = "{\"items\": [{\"id\": 1, \"name\": \"test\"}]}";
  Type type = new TypeToken<Wrapper<? extends Serializable>>(){}.getType();
  Wrapper<?> wrapper = new Gson().fromJson(json, type);
  assertNotNull(wrapper);
  assertNotNull(wrapper.items);
  assertEquals(1, wrapper.items.size());
}

private static class Wrapper<T> {
  List<T> items;
}

private static class SerializableItem implements Serializable {
  int id;
  String name;
}