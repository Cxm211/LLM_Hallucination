// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java::testTwoSmallDistanceUniques
@Test
    public void testTwoSmallDistanceUniques() {
        // Many identical points and two points that are slightly different.
        int[] repeatedArray = { 0 };
        int[] uniqueArray1 = { 1 };
        int[] uniqueArray2 = { 2 };
        CloseIntegerPoint repeatedPoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(repeatedArray));
        CloseIntegerPoint uniquePoint1 =
            new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray1));
        CloseIntegerPoint uniquePoint2 =
            new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray2));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        final int NUM_REPEATED_POINTS = 10 * 1000;
        for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
            points.add(repeatedPoint);
        }
        points.add(uniquePoint1);
        points.add(uniquePoint2);

        // Choose initial centers only.
        final long RANDOM_SEED = 0;
        final int NUM_CLUSTERS = 3;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        boolean unique1IsCenter = false;
        boolean unique2IsCenter = false;
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            if (cluster.getCenter().equals(uniquePoint1)) {
                unique1IsCenter = true;
            }
            if (cluster.getCenter().equals(uniquePoint2)) {
                unique2IsCenter = true;
            }
        }
        assertTrue(unique1IsCenter);
        assertTrue(unique2IsCenter);
    }