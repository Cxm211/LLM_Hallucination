// com/google/gson/stream/JsonWriterTest.java::testNonFiniteDoublesInObjectWhenLenient
public void testNonFiniteDoublesInObjectWhenLenient() throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter w = new JsonWriter(sw);
    w.setLenient(true);
    w.beginObject();
    w.name("a").value(Double.POSITIVE_INFINITY);
    w.name("b").value(Double.NEGATIVE_INFINITY);
    w.name("c").value(Double.NaN);
    w.endObject();
    assertEquals("{\"a\":Infinity,\"b\":-Infinity,\"c\":NaN}", sw.toString());
  }