// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_emptyJsonArray() throws IOException {
    JsonTreeReader in = new JsonTreeReader(new JsonArray());
    in.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, in.peek());
  }
