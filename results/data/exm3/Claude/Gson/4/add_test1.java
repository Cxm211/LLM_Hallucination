// com/google/gson/stream/JsonWriterTest.java
public void testTopLevelNumberValue() throws IOException {
    StringWriter string1 = new StringWriter();
    JsonWriter writer1 = new JsonWriter(string1);
    writer1.value(new Integer(456));
    writer1.close();
    assertEquals("456", string1.toString());

    StringWriter string2 = new StringWriter();
    JsonWriter writer2 = new JsonWriter(string2);
    writer2.value(new Double(789.0));
    writer2.close();
    assertEquals("789.0", string2.toString());
  }