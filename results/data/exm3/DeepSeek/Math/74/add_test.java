// org/apache/commons/math/ode/nonstiff/AdamsMoultonIntegratorTest.java
@Test
    public void testStepSizeNonZero() throws DerivativeException, IntegratorException {
        TestProblem1 pb = new TestProblem1();
        double range = Math.abs(pb.getFinalTime() - pb.getInitialTime());
        EmbeddedRungeKuttaIntegrator integ = new DormandPrince853Integrator(1.0e-6 * range, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        double[] y = new double[pb.getDimension()];
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(), pb.getFinalTime(), y);
        assertTrue(integ.getEvaluations() < 2000);
    }
