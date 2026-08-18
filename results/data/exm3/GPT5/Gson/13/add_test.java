// com/google/gson/stream/JsonReaderTest.java
public void testNegativeZeroTopLevel() throws Exception {
  JsonReader reader = new JsonReader(reader("-0"));
  reader.setLenient(false);
  assertEquals(NUMBER, reader.peek());
  assertEquals("-0", reader.nextString());
}