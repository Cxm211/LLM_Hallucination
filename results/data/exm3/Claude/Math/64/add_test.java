// org/apache/commons/math/optimization/general/MinpackTest.java
public void testMinpackBrownDennis() {
    minpackTest(new BrownDennisFunction(4, new double[] { 25.0, 5.0, -5.0, -1.0 },
                                        2815.43839161816, 292.954288244866,
                                        new double[] {
                                            -11.59125141003400,
                                            13.20363264963164,
                                            -0.40343948059624,
                                            0.23678636748425
                                        }), false);
}