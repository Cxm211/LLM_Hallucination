// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java::testDoubleRevert
@Test
    public void testDoubleRevert() {
        Line line = new Line(new Vector3D(1, 2, 3), new Vector3D(4, 5, 6));
        Vector3D expected = line.getDirection();

        Line revertedTwice = line.revert().revert();

        Assert.assertArrayEquals(expected.toArray(), revertedTwice.getDirection().toArray(), 0);
    }