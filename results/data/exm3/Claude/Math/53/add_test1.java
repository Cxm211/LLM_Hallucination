// org/apache/commons/math/complex/ComplexTest.java
@Test
public void testAddBothNaN() {
    Complex x = Complex.NaN;
    Complex y = Complex.NaN;
    Complex z = x.add(y);
    Assert.assertTrue(z.isNaN());
}