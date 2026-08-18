// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_primitiveBoolean() throws IOException {
  JsonTreeReader in = new JsonTreeReader(new JsonPrimitive(false));
  in.skipValue();
  assertEquals(JsonToken.END_DOCUMENT, in.peek());
}