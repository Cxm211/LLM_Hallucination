// com/google/gson/stream/JsonReaderTest.java
public void testEmptyArrayWithTrailingComma() throws IOException {
    JsonReader reader = new JsonReader(reader("[,]"));
    reader.setLenient(true);
    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();
    assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }
