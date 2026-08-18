// org/apache/commons/math/optimization/general/MinpackTest.java
public void testMinpackBrownBadlyScaled() {
    minpackTest(new BrownBadlyScaledFunction(new double[] { 1.0, 1.0 },
                                             0.0, 0.0,
                                             new double[] { 1.0e6, 2.0e-6 }), false);
  }
