// com/google/gson/stream/JsonWriterTest.java::testBoxedBooleans
public void testBoxedBooleanTopLevelNull() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.value((Boolean) null);
    assertEquals("null", stringWriter.toString());
  }