// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_jsonNull() throws IOException {
  JsonTreeReader in = new JsonTreeReader(JsonNull.INSTANCE);
  in.skipValue();
  assertEquals(JsonToken.END_DOCUMENT, in.peek());
}