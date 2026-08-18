// com/google/gson/DefaultDateTypeAdapterTest.java
public void testNullValueSqlDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
    assertNull(adapter.fromJson("null"));
    assertEquals("null", adapter.toJson(null));
  }
