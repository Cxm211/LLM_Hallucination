// com/google/gson/internal/bind/JsonTreeReaderTest.java::testNextName_singleEntryObject
public void testNextName_singleEntryObject() throws IOException {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);
    JsonTreeReader in = new JsonTreeReader(o);
    in.beginObject();
    assertEquals("a", in.nextName());
  }