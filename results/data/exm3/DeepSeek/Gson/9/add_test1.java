// com/google/gson/stream/JsonWriterTest.java
public void testBoxedBooleanInArrayWithNull() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value((Boolean) true);
    jsonWriter.value((Boolean) null);
    jsonWriter.value((Boolean) false);
    jsonWriter.endArray();
    assertEquals("[true,null,false]", stringWriter.toString());
  }
