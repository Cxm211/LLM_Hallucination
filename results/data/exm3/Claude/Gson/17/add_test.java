// com/google/gson/DefaultDateTypeAdapterTest.java
public void testNullValueWithTimestamp() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
    assertNull(adapter.fromJson("null"));
  }