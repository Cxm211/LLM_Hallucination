// org/apache/commons/math/ode/nonstiff/AdamsMoultonIntegratorTest.java
@Test
    public void testEventAtStart() throws DerivativeException, IntegratorException {
        TestProblem4 pb = new TestProblem4();
        double range = Math.abs(pb.getFinalTime() - pb.getInitialTime());
        EmbeddedRungeKuttaIntegrator integ = new DormandPrince853Integrator(1.0e-6 * range, range, 1.0e-12, 1.0e-12);
        integ.addEventHandler(new EventHandler() {
            public void eventOccurred(double t, double[] y, boolean increasing) {
                // do nothing
            }
            public void resetState(double t, double[] y) {
                // do nothing
            }
        }, 0.1, 1.0e-6, 100);
        double[] y = new double[pb.getDimension()];
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(), pb.getFinalTime(), y);
        // pass if no exception
    }
