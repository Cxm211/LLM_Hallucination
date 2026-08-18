// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java
@Test
    public void testRevertGeneral() {
        Line line = new Line(new Vector3D(1, 2, 3), new Vector3D(7, 8, 9));
        Vector3D expected = line.getDirection().negate();
        Line reverted = line.revert();
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 1e-10);
    }
