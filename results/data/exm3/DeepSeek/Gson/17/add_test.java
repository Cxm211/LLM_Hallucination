// com/google/gson/DefaultDateTypeAdapterTest.java
public void testNullValueTimestamp() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
    assertNull(adapter.fromJson("null"));
    assertEquals("null", adapter.toJson(null));
  }
