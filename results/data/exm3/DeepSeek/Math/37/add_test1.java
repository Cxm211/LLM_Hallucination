// org/apache/commons/math/complex/ComplexTest.java
@Test
    public void testTanhOverflow() {
        Complex z1 = new Complex(400, 0);
        Complex expected1 = new Complex(1, 0);
        TestUtils.assertEquals(expected1, z1.tanh(), 1.0e-5);
        Complex z2 = new Complex(-400, 0);
        Complex expected2 = new Complex(-1, 0);
        TestUtils.assertEquals(expected2, z2.tanh(), 1.0e-5);
    }
