// org/apache/commons/math3/util/MathArraysTest.java
@Test
    public void testLinearCombinationWithSingleElementArrayZero() {
        final double[] a = { 0.0 };
        final double[] b = { 123.456 };

        Assert.assertEquals(0.0, MathArrays.linearCombination(a, b), 0d);
    }