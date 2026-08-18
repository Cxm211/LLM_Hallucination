// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
    public void testAllIdenticalPoints() {
        // All points are identical - should still select k centers
        int[] identicalArray = { 5, 5, 5 };
        CloseIntegerPoint identicalPoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(identicalArray));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        final int NUM_POINTS = 100;
        for (int i = 0; i < NUM_POINTS; ++i) {
            points.add(identicalPoint);
        }

        final long RANDOM_SEED = 42;
        final int NUM_CLUSTERS = 3;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        // Should get k clusters even though all points are identical
        assertEquals(NUM_CLUSTERS, clusters.size());
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            assertEquals(identicalPoint, cluster.getCenter());
        }
    }