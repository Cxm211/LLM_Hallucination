// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testEqualsBothNegativeZero() {
    Complex c1 = new Complex(-0.0, -0.0);
    Complex c2 = new Complex(-0.0, -0.0);
    assertTrue(c1.equals(c2));
}