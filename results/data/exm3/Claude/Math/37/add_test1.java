// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testTanhLargeReal() {
    Complex z1 = new Complex(1E10, 0.0);
    Complex expected1 = new Complex(1.0, 0.0);
    TestUtils.assertEquals(expected1, z1.tanh(), 1.0e-5);
    
    Complex z2 = new Complex(-1E10, 0.0);
    Complex expected2 = new Complex(-1.0, 0.0);
    TestUtils.assertEquals(expected2, z2.tanh(), 1.0e-5);
    
    Complex z3 = new Complex(1E10, 2.5);
    Complex expected3 = new Complex(1.0, 0.0);
    TestUtils.assertEquals(expected3, z3.tanh(), 1.0e-5);
}