// com/google/gson/stream/JsonReaderTest.java
public void testEmptyObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }
