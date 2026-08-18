// org/joda/time/field/TestFieldUtils.java
public void testSafeMultiplyLongIntAdditional3() {
    try {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
        fail();
    } catch (ArithmeticException e) {
    }
}