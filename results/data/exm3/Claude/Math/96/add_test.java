// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testEqualsWithNegativeZero() {
    Complex c1 = new Complex(0.0, -0.0);
    Complex c2 = new Complex(0.0, 0.0);
    assertFalse(c1.equals(c2));
    assertFalse(c2.equals(c1));
}