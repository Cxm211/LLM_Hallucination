// org/apache/commons/math/ode/nonstiff/AdamsMoultonIntegratorTest.java
@Test
    public void polynomialBoundarySteps() throws DerivativeException, IntegratorException {
        TestProblem6 pb = new TestProblem6();
        double range = Math.abs(pb.getFinalTime() - pb.getInitialTime());

        // Test with nSteps = 3 (boundary case between high and low evaluations)
        AdamsMoultonIntegrator integ =
            new AdamsMoultonIntegrator(3, 1.0e-6 * range, 0.1 * range, 1.0e-9, 1.0e-9);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        assertTrue(integ.getEvaluations() > 140);
        
        // Test with nSteps = 4 (boundary case)
        integ = new AdamsMoultonIntegrator(4, 1.0e-6 * range, 0.1 * range, 1.0e-9, 1.0e-9);
        handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        assertTrue(integ.getEvaluations() < 90);
    }