// org/apache/commons/math/optimization/direct/BOBYQAOptimizerTest.java
@Test
    public void testConstrainedRosenWithExactlyMaxInterpolationPoints() {
        final int testDim = 5;
        final double[] startPoint = point(testDim, 0.1);
        final double[][] boundaries = boundaries(testDim, -1, 2);
        final RealPointValuePair expected = new RealPointValuePair(point(testDim, 1.0), 0.0);

        final int maxPoints = ((testDim + 1) * (testDim + 2)) / 2 - (2 * testDim + 1);

        doTest(new Rosen(), startPoint, boundaries,
               GoalType.MINIMIZE,
               1e-12, 1e-6, 2000,
               maxPoints,
               expected,
               "num=" + maxPoints);
    }