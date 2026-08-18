// com/google/gson/internal/bind/JsonTreeReaderTest.java
public void testSkipValue_filledJsonArray() throws IOException {
  JsonArray jsonArray = new JsonArray();
  jsonArray.add(1);
  jsonArray.add("string");
  jsonArray.add(true);
  JsonObject nested = new JsonObject();
  nested.addProperty("key", "value");
  jsonArray.add(nested);
  JsonTreeReader in = new JsonTreeReader(jsonArray);
  in.skipValue();
  assertEquals(JsonToken.END_DOCUMENT, in.peek());
}