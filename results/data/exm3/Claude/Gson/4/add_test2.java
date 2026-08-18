// com/google/gson/stream/JsonReaderTest.java
public void testTopLevelStringMultiplePeeks() throws IOException {
    JsonReader reader = new JsonReader(reader("\"hello\""));
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("hello", reader.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }