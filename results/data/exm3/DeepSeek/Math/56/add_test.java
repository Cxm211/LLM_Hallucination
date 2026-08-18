// org/apache/commons/math/util/MultidimensionalCounterTest.java
@Test
    public void testGetCountsSingleDimension() {
        final MultidimensionalCounter c = new MultidimensionalCounter(7);
        final int totalSize = c.getSize();
        for (int i = 0; i < totalSize; i++) {
            final int[] indices = c.getCounts(i);
            Assert.assertEquals(1, indices.length);
            Assert.assertEquals(i, indices[0]);
            Assert.assertEquals(i, c.getCount(indices));
        }
    }
