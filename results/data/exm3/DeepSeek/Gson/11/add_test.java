// com/google/gson/functional/PrimitiveTest.java
public void testNumberAsStringDeserializationVariousFormats() {
    Gson gson = new Gson();
    Number value = gson.fromJson(\"\\\"18\\\"\", Number.class);
    assertEquals(18, value.intValue());
    value = gson.fromJson(\"\\\"-18\\\"\", Number.class);
    assertEquals(-18, value.intValue());
    value = gson.fromJson(\"\\\"3.14\\\"\", Number.class);
    assertEquals(3.14, value.doubleValue(), 0.0001);
    value = gson.fromJson(\"\\\"1e2\\\"\", Number.class);
    assertEquals(100.0, value.doubleValue(), 0.0001);
    value = gson.fromJson(\"\\\"0\\\"\", Number.class);
    assertEquals(0, value.intValue());
    value = gson.fromJson(\"\\\"-0\\\"\", Number.class);
    assertEquals(0, value.intValue());
    value = gson.fromJson(\"\\\"123456789012345678901234567890\\\"\", Number.class);
    assertEquals(\"123456789012345678901234567890\", value.toString());
  }
