// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java
private static class GadgetWithBooleanPart {
    @JsonAdapter(BooleanPartAdapter.class)
    final boolean flag;
    GadgetWithBooleanPart(boolean flag) {
      this.flag = flag;
    }
  }
  private static class BooleanPartAdapter extends TypeAdapter<Boolean> {
    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value ? 1 : 0);
    }
    @Override
    public Boolean read(JsonReader in) throws IOException {
      int val = in.nextInt();
      return val != 0;
    }
  }
  public void testBooleanPrimitiveFieldAnnotation() {
    Gson gson = new Gson();
    String json = gson.toJson(new GadgetWithBooleanPart(true));
    assertEquals("{\"flag\":1}", json);
    GadgetWithBooleanPart gadget = gson.fromJson("{\"flag\":0}", GadgetWithBooleanPart.class);
    assertEquals(false, gadget.flag);
  }
