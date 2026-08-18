// com/google/gson/DefaultDateTypeAdapterTest.java
public void testUnexpectedTokenNumber() throws Exception {
    try {
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
      adapter.fromJson("123");
      fail("Unexpected token should fail.");
    } catch (JsonParseException expected) { }
  }