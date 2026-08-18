// org/apache/commons/math3/util/MathArraysTest.java
@Test
    public void testLinearCombinationWithSingleElementArrayNaN() {
        final double[] a = { Double.NaN };
        final double[] b = { 1.0 };
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(a, b)));
    }
