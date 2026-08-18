// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
public void testDistanceWithLargeValues() {
    int[] p1 = new int[] { 100000, 200000 };
    int[] p2 = new int[] { 50000, 150000 };
    double distance = EuclideanIntegerPoint.distance(p1, p2);
    double expected = Math.sqrt((long)(50000) * 50000 + (long)(50000) * 50000);
    assertEquals(expected, distance, 1e-9);
}