// org/apache/commons/math/ode/nonstiff/ClassicalRungeKuttaIntegratorTest.java::testStartEventImmediate
public void testStartEventImmediate() throws IntegratorException, DerivativeException {
      final double   t0     = 1878250320.0000029;
      final double[] k      = { 1.0e-4, 1.0e-5, 1.0e-6 };
      FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return k.length;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              for (int i = 0; i < y.length; ++i) {
                  yDot[i] = k[i] * y[i];
              }
          }
      };

      ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(60.0);

      double[] y0   = new double[k.length];
      for (int i = 0; i < y0.length; ++i) {
          y0[i] = i + 1;
      }
      final double[] y    = new double[k.length];

      final boolean[] called = new boolean[] { false };
      integrator.addEventHandler(new EventHandler() {

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - t0;
          }

          public int eventOccurred(double t, double[] y, boolean increasing) {
              called[0] = true;
              Assert.assertEquals(t0, t, 1.0e-9);
              return CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);

      double finalT = integrator.integrate(ode, t0, y0, t0 + 120.0, y);
      Assert.assertTrue(called[0]);
      Assert.assertEquals(t0 + 120.0, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * Math.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }
  }