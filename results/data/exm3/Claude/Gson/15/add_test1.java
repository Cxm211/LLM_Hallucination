// com/google/gson/stream/JsonWriterTest.java
public void testNegativeInfinityWhenNotLenient() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.beginArray();
  try {
    jsonWriter.value(Double.NEGATIVE_INFINITY);
    fail("Expected IllegalArgumentException");
  } catch (IllegalArgumentException expected) {
    assertTrue(expected.getMessage().contains("Numeric values must be finite"));
  }
}