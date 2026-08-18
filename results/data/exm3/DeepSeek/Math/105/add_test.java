// org/apache/commons/math/stat/regression/SimpleRegressionTest.java
public void testSSENonNegativeTwoPoints() {
        double[] x = {1.0000001, 2.0000002};
        double[] y = {2.0000002, 4.0000004};
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }
