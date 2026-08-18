// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java::testPrimitiveFieldAnnotationTakesPrecedenceOverDefault
public void testJsonAdapterOnObjectFieldTakesPrecedenceOverRuntimeType() {
    class ToStringAdapter extends com.google.gson.TypeAdapter<Object> {
      @Override public void write(com.google.gson.stream.JsonWriter out, Object value) throws java.io.IOException {
        if (value == null) { out.nullValue(); return; }
        out.value(String.valueOf(value));
      }
      @Override public Object read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
        String s = in.nextString();
        try { return Integer.valueOf(s); } catch (NumberFormatException e) { return s; }
      }
    }
    class Container {
      @com.google.gson.annotations.JsonAdapter(ToStringAdapter.class)
      Object value;
      Container(Object v) { this.value = v; }
    }
    com.google.gson.Gson gson = new com.google.gson.Gson();
    String json = gson.toJson(new Container(42));
    org.junit.Assert.assertEquals("{\"value\":\"42\"}", json);
    Container c = gson.fromJson(json, Container.class);
    org.junit.Assert.assertTrue(c.value instanceof Integer);
    org.junit.Assert.assertEquals(42, ((Integer) c.value).intValue());
  }