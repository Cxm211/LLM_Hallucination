// org/apache/commons/math/complex/ComplexTest.java
public void testEqualsBothSignedZero() {
    Complex c1 = new Complex(-0.0, -0.0);
    Complex c2 = new Complex(0.0, 0.0);
    assertEquals(c1, c2);
}
