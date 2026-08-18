// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
public void testDistanceWithNegativeValues() {
    int[] p1 = new int[] { -100000, -200000 };
    int[] p2 = new int[] { 100000, 200000 };
    double distance = EuclideanIntegerPoint.distance(p1, p2);
    double expected = Math.sqrt((long)(200000) * 200000 + (long)(400000) * 400000);
    assertEquals(expected, distance, 1e-9);
}