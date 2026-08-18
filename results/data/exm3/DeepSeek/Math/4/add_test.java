// org/apache/commons/math3/geometry/euclidean/twod/SubLineTest.java
@Test
    public void testIntersectionVarious() {
        // INSIDE both segments
        SubLine sub1 = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        SubLine sub2 = new SubLine(new Vector2D(0.5, -1), new Vector2D(0.5, 1));
        Vector2D intersection = sub1.intersection(sub2, true);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.5, intersection.getX(), 1.0e-10);
        Assert.assertEquals(0, intersection.getY(), 1.0e-10);
        intersection = sub1.intersection(sub2, false);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.5, intersection.getX(), 1.0e-10);
        Assert.assertEquals(0, intersection.getY(), 1.0e-10);

        // BOUNDARY: intersection at endpoint
        SubLine sub3 = new SubLine(new Vector2D(0, 0), new Vector2D(0, 1));
        intersection = sub1.intersection(sub3, true);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0, intersection.getX(), 1.0e-10);
        Assert.assertEquals(0, intersection.getY(), 1.0e-10);
        intersection = sub1.intersection(sub3, false);
        Assert.assertNull(intersection);

        // OUTSIDE: intersection point outside first segment
        SubLine sub4 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        intersection = sub1.intersection(sub4, true);
        Assert.assertNull(intersection);
        intersection = sub1.intersection(sub4, false);
        Assert.assertNull(intersection);

        // Parallel lines: no intersection
        SubLine sub5 = new SubLine(new Vector2D(0, 2), new Vector2D(1, 2));
        SubLine sub6 = new SubLine(new Vector2D(0, 3), new Vector2D(1, 3));
        intersection = sub5.intersection(sub6, true);
        Assert.assertNull(intersection);
        intersection = sub5.intersection(sub6, false);
        Assert.assertNull(intersection);
    }
