// com/google/gson/stream/JsonWriterTest.java
public void testBoxedBooleanObjectNull() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("bool");
    jsonWriter.value((Boolean) null);
    jsonWriter.endObject();
    assertEquals("{\"bool\":null}", stringWriter.toString());
  }
