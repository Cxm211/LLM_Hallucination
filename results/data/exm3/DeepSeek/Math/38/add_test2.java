// org/apache/commons/math/optimization/direct/BOBYQAOptimizerTest.java
@Test
    public void testPrelimIptGreaterThanN() {
        final int dim = 2;
        final double[] startPoint = point(dim, 0.1);
        final double[][] boundaries = boundaries(dim, -1, 1);
        final RealPointValuePair expected = new RealPointValuePair(point(dim, 1.0), 0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-12, 1e-6, 2000,
                2,
                expected,
                "ipt > n");
    }
