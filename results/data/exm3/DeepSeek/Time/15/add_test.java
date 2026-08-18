// org/joda/time/field/TestFieldUtils.java
public void testSafeMultiplyLongIntWithVal2MinusOne() {
        // val2 = -1
        assertEquals(0L, FieldUtils.safeMultiply(0L, -1));
        assertEquals(-1L, FieldUtils.safeMultiply(1L, -1));
        assertEquals(1L, FieldUtils.safeMultiply(-1L, -1));
        assertEquals(-Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1));
        // This should throw
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
            fail();
        } catch (ArithmeticException e) {
            // expected
        }
    }
