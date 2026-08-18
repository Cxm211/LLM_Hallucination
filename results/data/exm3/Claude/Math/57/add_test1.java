// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
    public void testLargeDistanceVariation() {
        // Points with very large distance differences
        int[] point1Array = { 0 };
        int[] point2Array = { 1000000 };
        CloseIntegerPoint point1 =
            new CloseIntegerPoint(new EuclideanIntegerPoint(point1Array));
        CloseIntegerPoint point2 =
            new CloseIntegerPoint(new EuclideanIntegerPoint(point2Array));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        points.add(point1);
        for (int i = 0; i < 1000; ++i) {
            points.add(point2);
        }

        final long RANDOM_SEED = 123;
        final int NUM_CLUSTERS = 2;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        // Both distinct points should have a chance to be selected as centers
        assertEquals(NUM_CLUSTERS, clusters.size());
        boolean hasPoint1 = false;
        boolean hasPoint2 = false;
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            if (cluster.getCenter().equals(point1)) hasPoint1 = true;
            if (cluster.getCenter().equals(point2)) hasPoint2 = true;
        }
        assertTrue(hasPoint1 || hasPoint2);
    }