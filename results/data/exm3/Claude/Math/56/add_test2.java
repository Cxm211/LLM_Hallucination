// org/apache/commons/math/util/MultidimensionalCounterTest.java
@Test
public void testGetCountsBoundaryTransitions() {
    final MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
    
    int[] indices = c.getCounts(2);
    Assert.assertArrayEquals(new int[]{0, 2}, indices);
    
    indices = c.getCounts(3);
    Assert.assertArrayEquals(new int[]{1, 0}, indices);
    
    indices = c.getCounts(4);
    Assert.assertArrayEquals(new int[]{1, 1}, indices);
}