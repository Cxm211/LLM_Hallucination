// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java
@Test
public void testDistanceWithIntegerOverflow() {
    int[] p1 = new int[] { Integer.MAX_VALUE, 0 };
    int[] p2 = new int[] { 0, 0 };
    double distance = EuclideanIntegerPoint.distance(p1, p2);
    double expected = Math.sqrt((long)Integer.MAX_VALUE * Integer.MAX_VALUE);
    assertEquals(expected, distance, 1e-9);
}