// org/apache/commons/math/util/MultidimensionalCounterTest.java
@Test
public void testGetCountsSingleDimension() {
    final MultidimensionalCounter c = new MultidimensionalCounter(5);
    for (int i = 0; i < 5; i++) {
        final int[] indices = c.getCounts(i);
        Assert.assertEquals(1, indices.length);
        Assert.assertEquals(i, indices[0]);
    }
}