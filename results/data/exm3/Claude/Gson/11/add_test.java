// com/google/gson/functional/PrimitiveTest.java
public void testNumberAsStringDeserializationWithDecimal() {
  Number value = gson.fromJson("\"3.14\"", Number.class);
  assertEquals(3.14, value.doubleValue(), 0.001);
}