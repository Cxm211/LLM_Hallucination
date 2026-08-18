// org/apache/commons/math/util/MultidimensionalCounterTest.java
@Test
public void testGetCountsFirstAndLastIndex() {
    final MultidimensionalCounter c = new MultidimensionalCounter(3, 4, 5);
    int[] indices = c.getCounts(0);
    Assert.assertArrayEquals(new int[]{0, 0, 0}, indices);
    
    indices = c.getCounts(59);
    Assert.assertArrayEquals(new int[]{2, 3, 4}, indices);
}