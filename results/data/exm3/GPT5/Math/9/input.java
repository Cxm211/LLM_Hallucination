// buggy function
    public Line revert() {
        final Line reverted = new Line(zero, zero.subtract(direction));
        return reverted;
    }

// trigger testcase
// org/apache/commons/math3/geometry/euclidean/threed/LineTest.java::testRevert
@Test
    public void testRevert() {
        
        // setup
        Line line = new Line(new Vector3D(1653345.6696423641, 6170370.041579291, 90000),
                             new Vector3D(1650757.5050732433, 6160710.879908984, 0.9));
        Vector3D expected = line.getDirection().negate();

        // action
        Line reverted = line.revert();

        // verify
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 0);

    }
