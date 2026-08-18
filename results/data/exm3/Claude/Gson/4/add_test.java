// com/google/gson/stream/JsonWriterTest.java
public void testTopLevelJsonValue() throws IOException {
    StringWriter string1 = new StringWriter();
    JsonWriter writer1 = new JsonWriter(string1);
    writer1.jsonValue("\"test\"");
    writer1.close();
    assertEquals("\"test\"", string1.toString());

    StringWriter string2 = new StringWriter();
    JsonWriter writer2 = new JsonWriter(string2);
    writer2.jsonValue("123");
    writer2.close();
    assertEquals("123", string2.toString());
  }