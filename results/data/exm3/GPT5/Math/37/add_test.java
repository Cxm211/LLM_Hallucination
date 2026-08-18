// org/apache/commons/math/complex/ComplexTest.java::testTan
@Test
    public void testTan() {
        Complex z = new Complex(3, 4);
        Complex expected = new Complex(-0.000187346, 0.999356);
        TestUtils.assertEquals(expected, z.tan(), 1.0e-5);
        /* Check that no overflow occurs (MATH-722) */
        Complex actual = new Complex(3.0, 1E10).tan();
        expected = new Complex(0, 1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(3.0, -1E10).tan();
        expected = new Complex(0, -1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        // Additional checks for large but finite imaginary parts
        actual = new Complex(3.0, 1000.0).tan();
        expected = new Complex(0, 1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
        actual = new Complex(3.0, -1000.0).tan();
        expected = new Complex(0, -1);
        TestUtils.assertEquals(expected, actual, 1.0e-5);
    }