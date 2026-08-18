// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java
@Test
public void testRevertNonZeroOrigin() {
    // Test revert with non-zero origin point
    Vector3D origin = new Vector3D(5, 10, 15);
    Vector3D direction = new Vector3D(1, 2, 3);
    Line line = new Line(origin, origin.add(direction));
    Line reverted = line.revert();
    
    // Verify origin is preserved
    Assert.assertArrayEquals(origin.toArray(), reverted.getOrigin().toArray(), 1e-10);
    
    // Verify direction is negated
    Vector3D expectedDirection = line.getDirection().negate();
    Assert.assertArrayEquals(expectedDirection.toArray(), reverted.getDirection().toArray(), 1e-10);
}