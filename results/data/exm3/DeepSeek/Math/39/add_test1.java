// org/apache/commons/math/ode/nonstiff/DormandPrince853IntegratorTest.java
@Test
  public void testVectorTolerances() {

      AdaptiveStepsizeIntegrator integ =
              new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
      final double start = 0.0;
      final double end   = 0.01;
      FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 2;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              Assert.assertTrue(t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
              Assert.assertTrue(t <= FastMath.nextAfter(end, Double.POSITIVE_INFINITY));
              yDot[0] = -10.0 * y[0];
              yDot[1] = -100.0 * y[1];
          }

      };

      double[] absTol = new double[] {1.0e-8, 1.0e-8};
      double[] relTol = new double[] {1.0e-6, 1.0e-6};
      integ.setStepSizeControl(absTol, relTol);
      integ.integrate(equations, start, new double[] { 1.0, 1.0 }, end, new double[2]);

  }
