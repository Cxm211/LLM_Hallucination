// com/google/gson/stream/JsonWriterTest.java
public void testBoxedBooleansInObject() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.beginObject();
  jsonWriter.name("a").value((Boolean) true);
  jsonWriter.name("b").value((Boolean) false);
  jsonWriter.name("c").value((Boolean) null);
  jsonWriter.endObject();
  assertEquals("{\"a\":true,\"b\":false,\"c\":null}", stringWriter.toString());
}