// org/apache/commons/math/ode/nonstiff/ClassicalRungeKuttaIntegratorTest.java
public void testBackwardMissedEndEvent() throws IntegratorException, DerivativeException {
    final double t0 = 10.0;
    final double tEvent = 5.0;
    final double[] k = { 1.0e-4 };
    FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
        public int getDimension() { return k.length; }
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = k[0] * y[0];
        }
    };

    ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(1.0);

    double[] y0 = new double[] { 1.0 };
    double[] y = new double[1];

    double finalT = integrator.integrate(ode, t0, y0, tEvent, y);
    Assert.assertEquals(tEvent, finalT, 1.0e-10);
    Assert.assertEquals(y0[0] * Math.exp(k[0] * (finalT - t0)), y[0], 1.0e-9);

    integrator.addEventHandler(new EventHandler() {
        public void resetState(double t, double[] y) { }
        public double g(double t, double[] y) {
            return t - tEvent;
        }
        public int eventOccurred(double t, double[] y, boolean increasing) {
            Assert.assertEquals(tEvent, t, 1.0e-10);
            return CONTINUE;
        }
    }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
    finalT = integrator.integrate(ode, t0, y0, tEvent - 5.0, y);
    Assert.assertEquals(tEvent - 5.0, finalT, 1.0e-10);
    Assert.assertEquals(y0[0] * Math.exp(k[0] * (finalT - t0)), y[0], 1.0e-9);
}
