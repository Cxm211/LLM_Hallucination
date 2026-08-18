// com/google/gson/stream/JsonReaderTest.java
public void testNextLongWithSingleQuotedValue() throws IOException {
    JsonReader reader = new JsonReader(reader("['789']"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(789L, reader.nextLong());
    reader.endArray();
  }