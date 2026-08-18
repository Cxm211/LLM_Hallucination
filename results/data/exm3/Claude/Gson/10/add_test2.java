// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java
public void testNullValueWithPrimitiveAnnotation() {
    Gson gson = new Gson();
    
    class NullableWrapper {
      @JsonAdapter(PartJsonFieldAnnotationAdapter.class)
      Integer value;
      
      NullableWrapper(Integer value) {
        this.value = value;
      }
    }
    
    String json = gson.toJson(new NullableWrapper(null));
    assertEquals("{}", json);
    NullableWrapper wrapper = gson.fromJson("{\"value\":null}", NullableWrapper.class);
    assertNull(wrapper.value);
  }