// org/apache/commons/math3/fraction/FractionTest.java::testIntegerOverflow2
@Test
public void testIntegerOverflow2() {
    // negative near-integer overflow case that should throw
    checkIntegerOverflow(-2147483649.0);
}