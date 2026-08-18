// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java
@Test
public void testRevertWithCustomTolerance() {
    // Test revert preserves custom tolerance
    double customTolerance = 1e-5;
    Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0), customTolerance);
    Line reverted = line.revert();
    
    // Verify direction is negated
    Assert.assertArrayEquals(new double[]{-1, 0, 0}, reverted.getDirection().toArray(), 1e-10);
    
    // Verify tolerance is preserved by checking that points at distance customTolerance are considered on the line
    Vector3D pointNearLine = new Vector3D(0, customTolerance * 0.9, 0);
    Assert.assertTrue(reverted.contains(pointNearLine));
    
    Vector3D pointFarFromLine = new Vector3D(0, customTolerance * 1.1, 0);
    Assert.assertFalse(reverted.contains(pointFarFromLine));
}