// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testAddNaNLeft() {
    Complex x = Complex.NaN;
    Complex y = new Complex(3.0, 4.0);
    Complex z = x.add(y);
    Assert.assertTrue(z.isNaN());
}