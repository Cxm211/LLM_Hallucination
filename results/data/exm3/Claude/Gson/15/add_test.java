// com/google/gson/stream/JsonWriterTest.java
public void testNonFiniteDoublesWhenNotLenient() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.setLenient(false);
  jsonWriter.beginArray();
  try {
    jsonWriter.value(Double.NaN);
    fail("Expected IllegalArgumentException");
  } catch (IllegalArgumentException expected) {
    assertTrue(expected.getMessage().contains("Numeric values must be finite"));
  }
}