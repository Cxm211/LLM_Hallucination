// com/google/gson/functional/DefaultTypeAdaptersTest.java
public void testJsonElementArrayTypeMismatch() {
  try {
    gson.fromJson("{\"key\":\"value\"}", JsonArray.class);
    fail();
  } catch (JsonSyntaxException expected) {
    assertEquals("Expected a com.google.gson.JsonArray but was com.google.gson.JsonObject",
        expected.getMessage());
  }
}