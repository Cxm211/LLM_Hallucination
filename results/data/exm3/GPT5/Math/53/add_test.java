// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testAddNaNRealPart() {
    Complex x = new Complex(3.0, 4.0);
    Complex z = new Complex(Double.NaN, 1.0);
    Complex w = x.add(z);
    Assert.assertTrue(Double.isNaN(w.getReal()));
    Assert.assertTrue(Double.isNaN(w.getImaginary()));
}