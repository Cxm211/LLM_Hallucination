// org/apache/commons/math/util/FastMathTest.java::testMinMaxFloat
@Test
public void testMaxSimple() {
    Assert.assertEquals(3.0f, FastMath.max(3.0f, 2.0f), 0.0f);
    Assert.assertEquals(3.0f, FastMath.max(2.0f, 3.0f), 0.0f);
}