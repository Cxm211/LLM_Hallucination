// org/apache/commons/math/util/MultidimensionalCounterTest.java
@Test
    public void testGetCountsTwoDims() {
        final MultidimensionalCounter c = new MultidimensionalCounter(3, 5);
        final int totalSize = c.getSize();
        for (int i = 0; i < totalSize; i++) {
            final int[] indices = c.getCounts(i);
            Assert.assertEquals(2, indices.length);
            Assert.assertEquals(i / 5, indices[0]);
            Assert.assertEquals(i % 5, indices[1]);
            Assert.assertEquals(i, c.getCount(indices));
        }
    }
