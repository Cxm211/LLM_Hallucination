// org/joda/time/field/TestFieldUtils.java
public void testSafeMultiplyLongIntAdditional2() {
    try {
        FieldUtils.safeMultiply(Long.MAX_VALUE, -2);
        fail();
    } catch (ArithmeticException e) {
    }
}