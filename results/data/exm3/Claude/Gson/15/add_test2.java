// com/google/gson/stream/JsonWriterTest.java
public void testPositiveInfinityWhenNotLenient() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.beginArray();
  try {
    jsonWriter.value(Double.POSITIVE_INFINITY);
    fail("Expected IllegalArgumentException");
  } catch (IllegalArgumentException expected) {
    assertTrue(expected.getMessage().contains("Numeric values must be finite"));
  }
}