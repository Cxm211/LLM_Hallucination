// org/apache/commons/math3/geometry/euclidean/threed/SubLineTest.java
@Test
    public void testIntersectionParallel3D() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine sub2 = new SubLine(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }