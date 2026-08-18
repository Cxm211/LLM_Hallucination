// com/google/gson/stream/JsonWriterTest.java
public void testNonFiniteDoubleTopLevelWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.value(Double.POSITIVE_INFINITY);
    assertEquals("Infinity", stringWriter.toString());
  }
