// com/google/gson/stream/JsonReaderTest.java
public void testNegativeZeroDouble() throws Exception {
    JsonReader reader = new JsonReader(reader("[-0]"));
    reader.setLenient(false);
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(-0.0, reader.nextDouble());
}
