// com/google/gson/stream/JsonReaderTest.java
public void testNextLongWithLargeLong() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[9007199254740993]"));
    reader.beginArray();
    assertEquals(9007199254740993L, reader.nextLong());
    reader.endArray();
  }
