// com/google/gson/DefaultDateTypeAdapterTest.java::testDateDeserializationISO8601
public void testDateDeserializationISO8601_NoMinutesZeroOffset() throws Exception {
  DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
  assertParsed("1970-01-01T00:00:00+00", adapter);
}