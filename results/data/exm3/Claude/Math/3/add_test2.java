// org/apache/commons/math3/util/MathArraysTest.java
@Test
    public void testLinearCombinationWithSingleElementArrayNegative() {
        final double[] a = { -2.5 };
        final double[] b = { 4.0 };

        Assert.assertEquals(-10.0, MathArrays.linearCombination(a, b), 0d);
    }