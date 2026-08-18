// com/google/gson/stream/JsonWriterTest.java
public void testBoxedBooleansMixed() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.beginArray();
  jsonWriter.value(true);
  jsonWriter.value((Boolean) false);
  jsonWriter.value((Boolean) null);
  jsonWriter.value((Boolean) true);
  jsonWriter.endArray();
  assertEquals("[true,false,null,true]", stringWriter.toString());
}