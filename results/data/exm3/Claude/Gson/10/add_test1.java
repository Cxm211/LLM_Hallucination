// com/google/gson/functional/JsonAdapterAnnotationOnFieldsTest.java
public void testFieldWithoutAnnotationUsesDefaultAdapter() {
    Gson gson = new Gson();
    
    class SimpleClass {
      int value;
      
      SimpleClass(int value) {
        this.value = value;
      }
    }
    
    String json = gson.toJson(new SimpleClass(99));
    assertEquals("{\"value\":99}", json);
    SimpleClass obj = gson.fromJson(json, SimpleClass.class);
    assertEquals(99, obj.value);
  }