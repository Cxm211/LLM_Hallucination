// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
    public void testDistanceOverflow() {
        EuclideanIntegerPoint p1 = new EuclideanIntegerPoint(new int[] {0});
        EuclideanIntegerPoint p2 = new EuclideanIntegerPoint(new int[] {50000});
        double distance = p1.distanceFrom(p2);
        assertFalse(Double.isNaN(distance));
        assertEquals(50000.0, distance, 1e-9);
    }
