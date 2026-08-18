// com/google/gson/functional/PrimitiveTest.java
public void testNumberAsStringDeserializationWithNegative() {
  Number value = gson.fromJson("\"-42\"", Number.class);
  assertEquals(-42, value.intValue());
}