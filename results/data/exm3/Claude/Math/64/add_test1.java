// org/apache/commons/math/optimization/general/MinpackTest.java
public void testMinpackHelicalValley() {
    minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                          50.0, 0.0,
                                          new double[] { 1.0, 0.0, 0.0 }), false);
    minpackTest(new HelicalValleyFunction(new double[] { 1.0, 1.0, 1.0 },
                                          25.78947916, 0.0,
                                          new double[] { 1.0, 0.0, 0.0 }), false);
}