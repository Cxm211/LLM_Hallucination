// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testAddNaNImaginaryPart() {
    Complex x = new Complex(2.0, 3.0);
    Complex y = new Complex(5.0, Double.NaN);
    Complex z = x.add(y);
    Assert.assertTrue(Double.isNaN(z.getReal()));
    Assert.assertTrue(Double.isNaN(z.getImaginary()));
}