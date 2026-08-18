// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java
private static class GadgetWithStringPart {
    @JsonAdapter(StringPartAdapter.class)
    final String part;
    GadgetWithStringPart(String part) {
      this.part = part;
    }
  }
  private static class StringPartAdapter extends TypeAdapter<String> {
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value(value.toUpperCase());
    }
    @Override
    public String read(JsonReader in) throws IOException {
      return in.nextString().toUpperCase();
    }
  }
  public void testNonPrimitiveFieldAnnotationTakesPrecedence() {
    Gson gson = new Gson();
    String json = gson.toJson(new GadgetWithStringPart("hello"));
    assertEquals("{\"part\":\"HELLO\"}", json);
    GadgetWithStringPart gadget = gson.fromJson("{\"part\":\"world\"}", GadgetWithStringPart.class);
    assertEquals("WORLD", gadget.part);
  }
