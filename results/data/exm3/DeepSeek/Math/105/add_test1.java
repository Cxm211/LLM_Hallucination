// org/apache/commons/math/stat/regression/SimpleRegressionTest.java
public void testSSENonNegativeLargeNumbers() {
        double[] x = {123456.789, 234567.891, 345678.912};
        double[] y = {2.5 * x[0], 2.5 * x[1], 2.5 * x[2]};
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }
