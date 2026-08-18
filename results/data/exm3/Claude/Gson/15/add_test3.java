// com/google/gson/stream/JsonWriterTest.java
public void testFiniteDoubleWhenLenient() throws IOException {
  StringWriter stringWriter = new StringWriter();
  JsonWriter jsonWriter = new JsonWriter(stringWriter);
  jsonWriter.setLenient(true);
  jsonWriter.beginArray();
  jsonWriter.value(3.14159);
  jsonWriter.value(0.0);
  jsonWriter.value(-2.71828);
  jsonWriter.endArray();
  assertEquals("[3.14159,0.0,-2.71828]", stringWriter.toString());
}