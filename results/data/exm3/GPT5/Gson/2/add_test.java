// com/google/gson/functional/DefaultTypeAdaptersTest.java::testJsonElementTypeMismatch
public void testJsonArrayTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonArray.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertEquals("Expected a com.google.gson.JsonArray but was com.google.gson.JsonPrimitive",
          expected.getMessage());
    }
  }