// com/google/gson/stream/JsonReaderTest.java
public void testEmptyArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[]"));
    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }
