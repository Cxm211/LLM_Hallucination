// com/google/gson/DefaultDateTypeAdapterTest.java
public void testDateDeserializationISO8601_NegativeOffset() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
    assertParsed("1970-01-01T01:00:00-01:00", adapter);
    assertParsed("1970-01-01T01:00:00-01", adapter);
}