// com/google/gson/DefaultDateTypeAdapterTest.java
public void testDateDeserializationISO8601_MultiDigitOffset() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
    assertParsed("1970-01-01T05:30:00+05:30", adapter);
    assertParsed("1970-01-01T12:00:00+12:00", adapter);
}