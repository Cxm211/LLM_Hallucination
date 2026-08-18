// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
    public void testFractionalDistances() {
        // Define points with fractional distances
        EuclideanDoublePoint pointA = new EuclideanDoublePoint(new double[]{0.0});
        EuclideanDoublePoint pointB = new EuclideanDoublePoint(new double[]{0.5});
        EuclideanDoublePoint pointC = new EuclideanDoublePoint(new double[]{1.0});
        Collection<EuclideanDoublePoint> points = new ArrayList<EuclideanDoublePoint>();
        for (int i = 0; i < 1000; i++) {
            points.add(pointA);
        }
        points.add(pointB);
        points.add(pointC);
        
        // Mock Random that picks first point as pointA (index 0) and then nextDouble returns 0.1
        Random mockRandom = new Random() {
            private int callCount = 0;
            @Override
            public int nextInt(int bound) {
                if (callCount++ == 0) {
                    // first center selection: return 0 to pick first point (pointA)
                    return 0;
                }
                // Should not be called for this test (since k=2, only one nextInt call)
                return super.nextInt(bound);
            }
            @Override
            public double nextDouble() {
                // This will be called when selecting second center
                return 0.1;
            }
        };
        
        KMeansPlusPlusClusterer<EuclideanDoublePoint> clusterer = 
            new KMeansPlusPlusClusterer<EuclideanDoublePoint>(mockRandom);
        List<Cluster<EuclideanDoublePoint>> clusters = clusterer.cluster(points, 2, 0);
        
        // Check that pointB is one of the centers
        boolean foundB = false;
        for (Cluster<EuclideanDoublePoint> cluster : clusters) {
            if (cluster.getCenter().equals(pointB)) {
                foundB = true;
                break;
            }
        }
        assertTrue(foundB);
    }
