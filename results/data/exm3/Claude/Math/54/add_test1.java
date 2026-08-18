// org/apache/commons/math/dfp/DfpTest.java
@Test
public void testSubnormalWithSign() {
    DfpField field = new DfpField(100);
    // Test small subnormal numbers close to zero with different signs
    double smallPos = Double.MIN_VALUE;
    double smallNeg = -Double.MIN_VALUE;
    Dfp dfpPos = field.newDfp(smallPos);
    Dfp dfpNeg = field.newDfp(smallNeg);
    double resultPos = dfpPos.toDouble();
    double resultNeg = dfpNeg.toDouble();
    Assert.assertTrue(resultPos > 0);
    Assert.assertTrue(resultNeg < 0);
    Assert.assertEquals(smallPos, resultPos, 0.0);
    Assert.assertEquals(smallNeg, resultNeg, 0.0);
}