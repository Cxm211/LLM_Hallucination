// org/apache/commons/math/optimization/general/LevenbergMarquardtOptimizerTest.java
public void testCircleFittingNonUniformWeights() throws FunctionEvaluationException, OptimizationException {
    Circle circle = new Circle();
    circle.addPoint(30.0, 68.0);
    circle.addPoint(50.0, -6.0);
    circle.addPoint(110.0, -20.0);
    LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    VectorialPointValuePair optimum = optimizer.optimize(circle, new double[] { 0, 0, 0 }, new double[] { 1, 2, 3 }, new double[] { 98.680, 47.345 });
    double rms = optimizer.getRMS();
    double expectedWeightedRMS = 0;
    double sumWeights = 0;
    for (int i = 0; i < 3; i++) {
        double weight = (i == 0) ? 1.0 : (i == 1) ? 2.0 : 3.0;
        expectedWeightedRMS += circle.getResiduals(optimum.getPointRef())[i] * circle.getResiduals(optimum.getPointRef())[i] * weight;
        sumWeights += weight;
    }
    expectedWeightedRMS = Math.sqrt(expectedWeightedRMS / sumWeights);
    assertEquals(expectedWeightedRMS, rms, 1.0e-10);
}