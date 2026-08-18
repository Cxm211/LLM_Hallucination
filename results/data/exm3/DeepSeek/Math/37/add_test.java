// org/apache/commons/math/complex/ComplexTest.java
@Test
    public void testTanOverflow() {
        Complex z1 = new Complex(0, 400);
        Complex expected1 = new Complex(0, 1);
        TestUtils.assertEquals(expected1, z1.tan(), 1.0e-5);
        Complex z2 = new Complex(0, -400);
        Complex expected2 = new Complex(0, -1);
        TestUtils.assertEquals(expected2, z2.tan(), 1.0e-5);
    }
