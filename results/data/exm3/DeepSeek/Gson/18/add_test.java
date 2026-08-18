// com/google/gson/functional/CollectionTest.java
public void testWildcardSupertype() {
    class WildcardContainer<T> {
      List<? extends T> items;
    }
    Gson gson = new Gson();
    String json = "{\"items\": [\"hello\", \"world\"]}";
    Type type = new TypeToken<WildcardContainer<String>>() {}.getType();
    WildcardContainer<String> container = gson.fromJson(json, type);
    assertNotNull(container.items);
    assertEquals(2, container.items.size());
    assertEquals(\"hello\", container.items.get(0));
    assertEquals(\"world\", container.items.get(1));
  }
