// org/apache/commons/math/optimization/general/LevenbergMarquardtOptimizerTest.java
public void testChiSquareWithNonUniformWeights() throws FunctionEvaluationException, OptimizationException {
        Circle circle = new Circle();
        circle.addPoint(0.0, 1.0);
        circle.addPoint(1.0, 0.0);
        circle.addPoint(0.0, -1.0);
        double[] weights = {2.0, 0.5, 3.0};
        double[] target = {0.0, 0.0, 0.0};
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(circle, target, weights, new double[] {0.0, 0.0});
        double chiSquare = optimizer.getChiSquare();
        double[] residuals = optimizer.getResiduals();
        double expected = 0.0;
        for (int i = 0; i < residuals.length; i++) {
            expected += residuals[i] * residuals[i] * weights[i];
        }
        assertEquals(expected, chiSquare, 1.0e-10);
    }
