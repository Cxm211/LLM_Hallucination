// org/apache/commons/math3/util/FastMathTest.java
@Test
    public void testCoshSinhThresholds() {
        double maxErr = 0;
        for (double x = -30.0; x <= 30.0; x += 0.01) {
            final double tstCosh = FastMath.cosh(x);
            final double refCosh = Math.cosh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(refCosh - tstCosh) / FastMath.ulp(refCosh));
            
            final double tstSinh = FastMath.sinh(x);
            final double refSinh = Math.sinh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(refSinh - tstSinh) / FastMath.ulp(refSinh));
        }
        Assert.assertEquals(0, maxErr, 3);
    }
