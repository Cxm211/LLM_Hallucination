// com/google/gson/regression/JsonAdapterNullSafeTest.java
public void testNullSafeFalseWithNullValue() throws Exception {
    String json = "{\"id\":null}";
    Device device = gson.fromJson(json, Device.class);
    assertNull(device.id);
  }