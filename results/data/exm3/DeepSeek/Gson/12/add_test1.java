// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_jsonPrimitive() throws IOException {
    JsonTreeReader in = new JsonTreeReader(new JsonPrimitive("test"));
    in.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, in.peek());
  }
