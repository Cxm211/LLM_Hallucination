// com/google/gson/DefaultDateTypeAdapterTest.java
public void testNullValueWithSqlDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
    assertNull(adapter.fromJson("null"));
  }