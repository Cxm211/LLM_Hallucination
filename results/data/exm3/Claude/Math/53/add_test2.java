// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testAddNaNRealPart() {
    Complex x = new Complex(2.0, 3.0);
    Complex y = new Complex(Double.NaN, 5.0);
    Complex z = x.add(y);
    Assert.assertTrue(Double.isNaN(z.getReal()));
    Assert.assertTrue(Double.isNaN(z.getImaginary()));
}