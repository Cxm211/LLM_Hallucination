// com/google/gson/DefaultDateTypeAdapterTest.java
public void testDateDeserializationISO8601NegativeOffsetNoMinutes() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
    assertParsed("1970-01-01T00:00:00-01", adapter);
  }
