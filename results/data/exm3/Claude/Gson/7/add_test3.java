// com/google/gson/stream/JsonReaderTest.java
public void testNextIntWithInvalidQuotedDouble() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"12.5\"]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
  }