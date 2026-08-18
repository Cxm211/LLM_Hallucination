// com/google/gson/stream/JsonReaderTest.java
public void testNegativeZeroInObject() throws Exception {
    JsonReader reader = new JsonReader(reader("{\"value\":-0}"));
    reader.setLenient(false);
    reader.beginObject();
    reader.nextName();
    assertEquals(NUMBER, reader.peek());
    assertEquals("-0", reader.nextString());
  }