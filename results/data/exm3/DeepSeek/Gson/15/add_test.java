// com/google/gson/stream/JsonWriterTest.java
public void testNonFiniteDoubleInObjectWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.beginObject();
    jsonWriter.name("value");
    jsonWriter.value(Double.NaN);
    jsonWriter.endObject();
    assertEquals("{\"value\":NaN}", stringWriter.toString());
  }
