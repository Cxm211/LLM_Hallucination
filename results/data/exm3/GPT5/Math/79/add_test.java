// org/apache/commons/math/stat/clustering/KMeansPlusPlusClustererTest.java::testDistanceOverflow
@Test
public void testDistanceOverflow() {
    assertEquals(50000.0, distance(new int[]{0}, new int[]{50000}), 1e-9);
}