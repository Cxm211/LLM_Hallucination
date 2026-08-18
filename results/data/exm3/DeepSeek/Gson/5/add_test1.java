// com/google/gson/DefaultDateTypeAdapterTest.java
public void testDateDeserializationISO8601ZeroOffsetNoMinutes() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
    assertParsed("1970-01-01T00:00:00+00", adapter);
  }
