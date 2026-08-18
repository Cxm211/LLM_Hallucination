// com/google/gson/DefaultDateTypeAdapterTest.java
public void testUnexpectedNumberToken() throws Exception {
  try {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    adapter.fromJson("5");
    fail("Unexpected token should fail.");
  } catch (IllegalStateException expected) { }
}