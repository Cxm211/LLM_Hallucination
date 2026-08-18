// org/apache/commons/math3/optim/nonlinear/scalar/gradient/NonLinearConjugateGradientOptimizerTest.java
@Test
public void testIterationsMultipleSteps() {
    LinearProblem problem = new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { 5, 7 });
    NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
        NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
        new SimpleValueChecker(1e-6, 1e-6));
    PointValuePair optimum = optimizer.optimize(
        new MaxEval(100),
        problem.getObjectiveFunction(),
        problem.getObjectiveFunctionGradient(),
        GoalType.MINIMIZE,
        new InitialGuess(new double[] { 0, 0 }));
    Assert.assertEquals(5.0, optimum.getPoint()[0], 1.0e-10);
    Assert.assertEquals(7.0, optimum.getPoint()[1], 1.0e-10);
    Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);
    Assert.assertTrue(optimizer.getIterations() > 0);
}