// org/apache/commons/math3/util/MathArraysTest.java
@Test
    public void testLinearCombinationWithSingleElementArrayInfinity() {
        final double[] a = { Double.POSITIVE_INFINITY };
        final double[] b = { 2.0 };
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathArrays.linearCombination(a, b), 0d);
    }
