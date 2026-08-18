// com/google/gson/functional/DefaultTypeAdaptersTest.java
public void testJsonElementSubtypeDeserialization() {
  try {
    gson.fromJson("[1,2,3]", JsonPrimitive.class);
    fail();
  } catch (JsonSyntaxException expected) {
    assertEquals("Expected a com.google.gson.JsonPrimitive but was com.google.gson.JsonArray",
        expected.getMessage());
  }
}