// org/apache/commons/math/distribution/NormalDistributionTest.java::testMath280
public void testBracketExpansion() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 100.0;
            }
        };
        // With unit-step expansion and maxIterations=50, original buggy code fails to bracket 100 starting from 0.
        double[] br = UnivariateRealSolverUtils.bracket(f, 0.0, -1000.0, 1000.0, 50);
        assertTrue("Bracket should contain the root 100", br[0] <= 100.0 && br[1] >= 100.0);
    }