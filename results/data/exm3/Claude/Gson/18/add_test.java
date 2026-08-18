// com/google/gson/functional/CollectionTest.java
public void testWildcardTypeWithLowerBound() {
  String json = "{\"data\": [{\"value\": \"test\"}]}";
  Type type = new TypeToken<Container<? super Item>>(){}.getType();
  Container<?> container = new Gson().fromJson(json, type);
  assertNotNull(container);
  assertNotNull(container.data);
  assertEquals(1, container.data.size());
}

private static class Container<T> {
  List<T> data;
}

private static class Item {
  String value;
}