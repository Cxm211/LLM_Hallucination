// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java
public void testNonPrimitiveFieldAnnotationTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    
    class Wrapper {
      @JsonAdapter(PartJsonFieldAnnotationAdapter.class)
      Integer value;
      
      Wrapper(Integer value) {
        this.value = value;
      }
    }
    
    String json = gson.toJson(new Wrapper(100));
    assertEquals("{\"value\":\"100\"}", json);
    Wrapper wrapper = gson.fromJson(json, Wrapper.class);
    assertEquals(Integer.valueOf(100), wrapper.value);
  }