// com/google/gson/stream/JsonReaderTest.java::testNextLongOnUnquotedStringPrefixedWithInteger
public void testNextLongOnUnquotedStringPrefixedWithInteger() throws IOException {
    JsonReader reader = new JsonReader(reader("[123xyz]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals("123xyz", reader.nextString());
  }