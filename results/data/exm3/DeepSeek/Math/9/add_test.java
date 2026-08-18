// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java
@Test
    public void testRevertSimple() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Vector3D expected = line.getDirection().negate();
        Line reverted = line.revert();
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 1e-10);
    }
