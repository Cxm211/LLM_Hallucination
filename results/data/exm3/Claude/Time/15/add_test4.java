// org/joda/time/field/TestFieldUtils.java
public void testSafeMultiplyLongIntAdditional5() {
    assertEquals(0L, FieldUtils.safeMultiply(0L, Integer.MIN_VALUE));
    assertEquals(0L, FieldUtils.safeMultiply(0L, Integer.MAX_VALUE));
    assertEquals(0L, FieldUtils.safeMultiply(0L, -100));
}