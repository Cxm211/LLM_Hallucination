// com/google/gson/functional/PrimitiveTest.java
public void testNumberAsStringDeserializationWithScientificNotation() {
  Number value = gson.fromJson("\"1.5e10\"", Number.class);
  assertEquals(1.5e10, value.doubleValue(), 0.001);
}