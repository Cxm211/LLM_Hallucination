// org/apache/commons/math/util/MultidimensionalCounterTest.java::testOneDimensionalCounts
@Test
    public void testOneDimensionalCounts() {
        final MultidimensionalCounter c = new MultidimensionalCounter(5);
        final int totalSize = c.getSize();
        for (int i = 0; i < totalSize; i++) {
            Assert.assertArrayEquals(new int[] { i }, c.getCounts(i));
            Assert.assertEquals(i, c.getCount(new int[] { i }));
        }
    }