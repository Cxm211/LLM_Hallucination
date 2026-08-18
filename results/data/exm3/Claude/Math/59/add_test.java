// org/apache/commons/math/util/FastMathTest.java
@Test
public void testMaxFloatEdgeCases() {
    Assert.assertEquals("max(5.0f, 3.0f)", 5.0f, FastMath.max(5.0f, 3.0f), MathUtils.EPSILON);
    Assert.assertEquals("max(3.0f, 5.0f)", 5.0f, FastMath.max(3.0f, 5.0f), MathUtils.EPSILON);
    Assert.assertEquals("max(0.0f, -0.0f)", 0.0f, FastMath.max(0.0f, -0.0f), MathUtils.EPSILON);
    Assert.assertEquals("max(-0.0f, 0.0f)", 0.0f, FastMath.max(-0.0f, 0.0f), MathUtils.EPSILON);
    Assert.assertTrue("max(Float.NaN, Float.NaN) should be NaN", Float.isNaN(FastMath.max(Float.NaN, Float.NaN)));
    Assert.assertEquals("max(Float.MAX_VALUE, Float.MIN_VALUE)", Float.MAX_VALUE, FastMath.max(Float.MAX_VALUE, Float.MIN_VALUE), MathUtils.EPSILON);
}