// com/google/gson/stream/JsonReaderTest.java
public void testNextIntWithQuotedValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"456\"]"));
    reader.beginArray();
    assertEquals(456, reader.nextInt());
    reader.endArray();
  }