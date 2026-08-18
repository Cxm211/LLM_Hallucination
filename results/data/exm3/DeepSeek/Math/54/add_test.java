// org/apache/commons/math/dfp/DfpTest.java
@Test
    public void testNegativeSmallNumber() {
        DfpField field = new DfpField(100);
        double smallNeg = -Double.MIN_VALUE;
        Dfp dfp = field.newDfp(smallNeg);
        double result = dfp.toDouble();
        Assert.assertEquals(-1, FastMath.copySign(1, result), MathUtils.EPSILON);
    }
