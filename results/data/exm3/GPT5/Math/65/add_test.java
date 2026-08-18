// org/apache/commons/math/optimization/general/LevenbergMarquardtOptimizerTest.java::testRMSAndChiSquareWeighting
public void testRMSAndChiSquareWeighting() throws Exception {
        org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer optimizer = new org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer();

        org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction f = new org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double p = point[0];
                return new double[] { p, p, p };
            }
            public org.apache.commons.math.analysis.MultivariateMatrixFunction jacobian() {
                return new org.apache.commons.math.analysis.MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 }, { 1.0 }, { 1.0 } };
                    }
                };
            }
        };

        double[] target = new double[] { 0.0, 1.0, 2.0 };
        double[] weights = new double[] { 1.0, 2.0, 3.0 };
        double[] start = new double[] { 0.0 };

        optimizer.optimize(f, target, weights, start);

        // Expected optimal parameter p* = weighted mean of target values
        double wSum = 0.0;
        double wtSum = 0.0;
        for (int i = 0; i < target.length; ++i) {
            wSum += weights[i];
            wtSum += weights[i] * target[i];
        }
        double pStar = wtSum / wSum;

        double chiExp = 0.0;
        double rss = 0.0;
        for (int i = 0; i < target.length; ++i) {
            double r = pStar - target[i];
            chiExp += weights[i] * r * r;
            rss += r * r;
        }
        double rmsExp = Math.sqrt(rss / target.length);

        assertEquals(rmsExp, optimizer.getRMS(), 1.0e-12);
        assertEquals(chiExp, optimizer.getChiSquare(), 1.0e-12);
    }