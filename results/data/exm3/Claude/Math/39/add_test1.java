// org/apache/commons/math/ode/nonstiff/DormandPrince853IntegratorTest.java
@Test
  public void testTooLargeFirstStepWithVectorTolerances() {

      AdaptiveStepsizeIntegrator integ =
              new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, new double[] { Double.NaN }, new double[] { Double.NaN });
      final double start = 0.0;
      final double end   = 0.001;
      FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              Assert.assertTrue(t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
              Assert.assertTrue(t <= FastMath.nextAfter(end,   Double.POSITIVE_INFINITY));
              yDot[0] = -100.0 * y[0];
          }

      };

      integ.setStepSizeControl(0, 1.0, new double[] { 1.0e-6 }, new double[] { 1.0e-8 });
      integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);

  }