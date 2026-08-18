// org/apache/commons/math/stat/regression/SimpleRegressionTest.java::testSSENonNegativeScaled
public void testSSENonNegativeScaled() {
        double[] y = { 89151.02, 89193.02, 89235.02 };
        double[] x = { 1.107178495E3, 1.107264895E3, 1.107351295E3 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }