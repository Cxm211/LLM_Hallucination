// org/apache/commons/math/stat/inference/ChiSquareTestTest.java::testChiSquare
        // additional scaling check: expected and observed totals differ
        long[] observed2 = {30, 20};
        double[] expected2 = {10.0, 30.0}; // sums to 40 vs observed sum 50
        assertEquals("chi-square statistic with scaling", 32.666666666666664, testStatistic.chiSquare(expected2, observed2), 1E-12);