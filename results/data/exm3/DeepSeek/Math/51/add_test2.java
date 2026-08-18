// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
@Test
    public void testPegasusBelowSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                @Override
                public double value(double x) {
                    return Math.cos(x) - 0.5;
                }
            };
        final PegasusSolver solver = new PegasusSolver();
        solver.setAllowedSolution(AllowedSolution.BELOW_SIDE);
        final double root = solver.solve(100, f, 0.0, 2.0);
        final double exact = Math.acos(0.5);
        Assert.assertEquals(exact, root, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.computeObjectiveValue(root) <= 0.0);
    }
