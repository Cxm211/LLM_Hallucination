// com/google/gson/stream/JsonReaderTest.java
public void testNextLongWithQuotedValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"123\"]"));
    reader.beginArray();
    assertEquals(123L, reader.nextLong());
    reader.endArray();
  }