// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_primitiveNumber() throws IOException {
  JsonTreeReader in = new JsonTreeReader(new JsonPrimitive(42));
  in.skipValue();
  assertEquals(JsonToken.END_DOCUMENT, in.peek());
}