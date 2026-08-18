// org/apache/commons/math/optimization/general/MinpackTest.java
public void testMinpackBard() {
    minpackTest(new BardFunction(new double[] { 1.0, 1.0, 1.0 },
                                 8.21487e-3, 0.090635,
                                 new double[] { 0.08241, 1.1330, 2.3437 }), false);
  }
