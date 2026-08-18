// com/google/gson/DefaultDateTypeAdapterTest.java
public void testUnexpectedTokenArray() throws Exception {
    try {
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
      adapter.fromJson("[]");
      fail("Unexpected token should fail.");
    } catch (IllegalStateException expected) { }
  }