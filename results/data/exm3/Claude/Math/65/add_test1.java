// org/apache/commons/math/optimization/general/LevenbergMarquardtOptimizerTest.java
public void testCircleFittingAllWeightsEqual() throws FunctionEvaluationException, OptimizationException {
    Circle circle = new Circle();
    circle.addPoint(30.0, 68.0);
    circle.addPoint(50.0, -6.0);
    circle.addPoint(110.0, -20.0);
    circle.addPoint(35.0, 15.0);
    LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    double[] weights = new double[] { 5.0, 5.0, 5.0, 5.0 };
    VectorialPointValuePair optimum = optimizer.optimize(circle, new double[] { 0, 0, 0, 0 }, weights, new double[] { 98.680, 47.345 });
    double rms = optimizer.getRMS();
    double expectedWeightedRMS = 0;
    for (int i = 0; i < 4; i++) {
        expectedWeightedRMS += circle.getResiduals(optimum.getPointRef())[i] * circle.getResiduals(optimum.getPointRef())[i] * 5.0;
    }
    expectedWeightedRMS = Math.sqrt(expectedWeightedRMS / (4 * 5.0));
    assertEquals(expectedWeightedRMS, rms, 1.0e-10);
}