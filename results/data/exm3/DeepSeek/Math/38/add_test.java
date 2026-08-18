// org/apache/commons/math/optimization/direct/BOBYQAOptimizerTest.java
@Test
    public void testPrelimEqualBounds() {
        final int dim = 2;
        final double[] startPoint = point(dim, 0.0);
        final double[][] boundaries = new double[dim][2];
        boundaries[0][0] = 0.0;
        boundaries[0][1] = 0.0;
        boundaries[1][0] = -1.0;
        boundaries[1][1] = 1.0;
        final RealPointValuePair expected = new RealPointValuePair(point(dim, 0.0), 0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-12, 1e-6, 2000,
                0,
                expected,
                "equal bounds");
    }
