// org/joda/time/field/TestFieldUtils.java
public void testSafeMultiplyLongIntAdditional4() {
    try {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -2);
        fail();
    } catch (ArithmeticException e) {
    }
}