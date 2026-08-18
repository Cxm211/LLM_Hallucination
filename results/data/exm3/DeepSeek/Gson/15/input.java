// buggy function
  public JsonWriter value(double value) throws IOException {
    writeDeferredName();
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Double.toString(value));
    return this;
  }

// trigger testcase
// com/google/gson/stream/JsonWriterTest.java::testNonFiniteDoublesWhenLenient
public void testNonFiniteDoublesWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Double.NaN);
    jsonWriter.value(Double.NEGATIVE_INFINITY);
    jsonWriter.value(Double.POSITIVE_INFINITY);
    jsonWriter.endArray();
    assertEquals("[NaN,-Infinity,Infinity]", stringWriter.toString());
  }
