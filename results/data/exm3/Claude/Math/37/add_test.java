// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testTanLargeImaginary() {
    Complex z1 = new Complex(0.0, 1E10);
    Complex expected1 = new Complex(0.0, 1.0);
    TestUtils.assertEquals(expected1, z1.tan(), 1.0e-5);
    
    Complex z2 = new Complex(0.0, -1E10);
    Complex expected2 = new Complex(0.0, -1.0);
    TestUtils.assertEquals(expected2, z2.tan(), 1.0e-5);
    
    Complex z3 = new Complex(2.5, 1E10);
    Complex expected3 = new Complex(0.0, 1.0);
    TestUtils.assertEquals(expected3, z3.tan(), 1.0e-5);
}