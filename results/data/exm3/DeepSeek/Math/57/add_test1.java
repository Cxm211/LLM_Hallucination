// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
    public void testAllZeroDistances() {
        // Custom point class with zero distance but distinct ids
        class ZeroDistancePoint implements Clusterable<ZeroDistancePoint> {
            private final int id;
            ZeroDistancePoint(int id) { this.id = id; }
            public double distanceFrom(ZeroDistancePoint other) { return 0.0; }
            public int getId() { return id; }
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                ZeroDistancePoint that = (ZeroDistancePoint) obj;
                return id == that.id;
            }
            @Override
            public int hashCode() { return id; }
        }
        
        List<ZeroDistancePoint> points = new ArrayList<ZeroDistancePoint>();
        for (int i = 0; i < 10; i++) {
            points.add(new ZeroDistancePoint(i));
        }
        final int[] callCount = new int[1];
        Random mockRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                if (callCount[0] == 0) {
                    callCount[0]++;
                    // first center selection, bound = 10
                    return 5; // pick point with id 5
                } else {
                    // second center selection, bound = 9 (after removal)
                    return 3; // pick point with id 3 (original index 3)
                }
            }
        };
        KMeansPlusPlusClusterer<ZeroDistancePoint> clusterer = 
            new KMeansPlusPlusClusterer<ZeroDistancePoint>(mockRandom);
        List<Cluster<ZeroDistancePoint>> clusters = clusterer.cluster(points, 2, 0);
        assertEquals(2, clusters.size());
        Set<Integer> centerIds = new HashSet<Integer>();
        for (Cluster<ZeroDistancePoint> cluster : clusters) {
            centerIds.add(cluster.getCenter().getId());
        }
        assertTrue(centerIds.contains(5));
        assertTrue(centerIds.contains(3));
    }
