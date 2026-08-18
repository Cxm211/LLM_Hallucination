// com/google/gson/functional/PrimitiveTest.java::testNumberAsStringDeserialization
public void testDecimalNumberAsStringDeserialization() {
    Number value = gson.fromJson("\"3.14\"", Number.class);
    assertEquals(3.14, value.doubleValue(), 0.0);
  }