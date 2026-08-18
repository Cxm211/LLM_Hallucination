// org/apache/commons/math/ode/nonstiff/AdamsMoultonIntegratorTest.java::polynomial
@Test
    public void vectorTolerancesAffectInitialization() throws DerivativeException, IntegratorException {
        // y' = y, y(0) = 1000, solution y(t) = 1000 * exp(t)
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };

        double t0 = 0.0;
        double[] y0 = new double[] { 1000.0 };
        double t1 = 1.0;
        double[] y = new double[1];

        double[] vecAbs = new double[] { 1.0e-6 };
        double[] vecRel = new double[] { 1.0e-3 };

        for (int nSteps = 4; nSteps <= 6; ++nSteps) {
            AdamsMoultonIntegrator integ = new AdamsMoultonIntegrator(nSteps, 1.0e-8, 1.0, vecAbs, vecRel);
            integ.integrate(eq, t0, y0, t1, y);
            // With proper use of relative tolerance, the number of evaluations should remain moderate
            assertTrue("Too many evaluations with vector tolerances for nSteps=" + nSteps, integ.getEvaluations() < 120);
        }
    }