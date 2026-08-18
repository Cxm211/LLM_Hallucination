// org/apache/commons/math3/util/FastMathTest.java
@Test
public void testCoshSinhBoundary() {
    // Test values at the boundary threshold of 20
    double x1 = 20.0;
    double cosh1 = FastMath.cosh(x1);
    double ref1 = Math.cosh(x1);
    Assert.assertEquals(ref1, cosh1, FastMath.ulp(ref1) * 3);

    double sinh1 = FastMath.sinh(x1);
    double sinhRef1 = Math.sinh(x1);
    Assert.assertEquals(sinhRef1, sinh1, FastMath.ulp(sinhRef1) * 3);

    // Test values at the negative boundary threshold of -20
    double x2 = -20.0;
    double cosh2 = FastMath.cosh(x2);
    double ref2 = Math.cosh(x2);
    Assert.assertEquals(ref2, cosh2, FastMath.ulp(ref2) * 3);

    double sinh2 = FastMath.sinh(x2);
    double sinhRef2 = Math.sinh(x2);
    Assert.assertEquals(sinhRef2, sinh2, FastMath.ulp(sinhRef2) * 3);

    // Test values slightly above 20
    double x3 = 20.5;
    double cosh3 = FastMath.cosh(x3);
    double ref3 = Math.cosh(x3);
    Assert.assertEquals(ref3, cosh3, FastMath.ulp(ref3) * 3);

    double sinh3 = FastMath.sinh(x3);
    double sinhRef3 = Math.sinh(x3);
    Assert.assertEquals(sinhRef3, sinh3, FastMath.ulp(sinhRef3) * 3);

    // Test values slightly below -20
    double x4 = -20.5;
    double cosh4 = FastMath.cosh(x4);
    double ref4 = Math.cosh(x4);
    Assert.assertEquals(ref4, cosh4, FastMath.ulp(ref4) * 3);

    double sinh4 = FastMath.sinh(x4);
    double sinhRef4 = Math.sinh(x4);
    Assert.assertEquals(sinhRef4, sinh4, FastMath.ulp(sinhRef4) * 3);
}