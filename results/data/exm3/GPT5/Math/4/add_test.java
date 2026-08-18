// org/apache/commons/math3/geometry/euclidean/threed/SubLineTest.java
@Test
public void testIntersectionColinearDisjoint() throws MathIllegalArgumentException {
    // Two colinear segments on the same infinite line but disjoint
    SubLine sub1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
    SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(3, 0, 0));
    Assert.assertNull(sub1.intersection(sub2, true));
    Assert.assertNull(sub1.intersection(sub2, false));
}