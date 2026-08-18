// org/apache/commons/math3/geometry/euclidean/twod/SubLineTest.java
@Test
    public void testIntersectionParallelVertical() {
        final SubLine sub1 = new SubLine(new Vector2D(1, 0), new Vector2D(1, 5));
        final SubLine sub2 = new SubLine(new Vector2D(3, 0), new Vector2D(3, 5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }