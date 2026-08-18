// buggy code
    public Vector3D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // compute the intersection on infinite line
        Vector3D v1D = line.intersection(subLine.line);

        // check location of point with respect to first sub-line
        Location loc1 = remainingRegion.checkPoint(line.toSubSpace(v1D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.remainingRegion.checkPoint(subLine.line.toSubSpace(v1D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v1D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v1D : null;
        }

    }

    public Vector2D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // retrieve the underlying lines
        Line line1 = (Line) getHyperplane();
        Line line2 = (Line) subLine.getHyperplane();

        // compute the intersection on infinite line
        Vector2D v2D = line1.intersection(line2);

        // check location of point with respect to first sub-line
        Location loc1 = getRemainingRegion().checkPoint(line1.toSubSpace(v2D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.getRemainingRegion().checkPoint(line2.toSubSpace(v2D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v2D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v2D : null;
        }

    }

// relevant test
// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testBox
    public void testBox() {
        PolyhedronsSet tree = new PolyhedronsSet(0, 1, 0, 1, 0, 1);
        Assert.assertEquals(1.0, tree.getSize(), 1.0e-10);
        Assert.assertEquals(6.0, tree.getBoundarySize(), 1.0e-10);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(0.5, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(0.5, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(0.5, barycenter.getZ(), 1.0e-10);
        for (double x = -0.25; x < 1.25; x += 0.1) {
            boolean xOK = (x >= 0.0) && (x <= 1.0);
            for (double y = -0.25; y < 1.25; y += 0.1) {
                boolean yOK = (y >= 0.0) && (y <= 1.0);
                for (double z = -0.25; z < 1.25; z += 0.1) {
                    boolean zOK = (z >= 0.0) && (z <= 1.0);
                    Region.Location expected =
                        (xOK && yOK && zOK) ? Region.Location.INSIDE : Region.Location.OUTSIDE;
                    Assert.assertEquals(expected, tree.checkPoint(new Vector3D(x, y, z)));
                }
            }
        }
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            new Vector3D(0.0, 0.5, 0.5),
            new Vector3D(1.0, 0.5, 0.5),
            new Vector3D(0.5, 0.0, 0.5),
            new Vector3D(0.5, 1.0, 0.5),
            new Vector3D(0.5, 0.5, 0.0),
            new Vector3D(0.5, 0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            new Vector3D(0.0, 1.2, 1.2),
            new Vector3D(1.0, 1.2, 1.2),
            new Vector3D(1.2, 0.0, 1.2),
            new Vector3D(1.2, 1.0, 1.2),
            new Vector3D(1.2, 1.2, 0.0),
            new Vector3D(1.2, 1.2, 1.0)
        });
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testTetrahedron
    public void testTetrahedron() throws MathArithmeticException {
        Vector3D vertex1 = new Vector3D(1, 2, 3);
        Vector3D vertex2 = new Vector3D(2, 2, 4);
        Vector3D vertex3 = new Vector3D(2, 3, 3);
        Vector3D vertex4 = new Vector3D(1, 3, 4);
        @SuppressWarnings("unchecked")
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1),
                new Plane(vertex2, vertex3, vertex4),
                new Plane(vertex4, vertex3, vertex1),
                new Plane(vertex1, vertex2, vertex4));
        Assert.assertEquals(1.0 / 3.0, tree.getSize(), 1.0e-10);
        Assert.assertEquals(2.0 * FastMath.sqrt(3.0), tree.getBoundarySize(), 1.0e-10);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(1.5, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(2.5, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(3.5, barycenter.getZ(), 1.0e-10);
        double third = 1.0 / 3.0;
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            vertex1, vertex2, vertex3, vertex4,
            new Vector3D(third, vertex1, third, vertex2, third, vertex3),
            new Vector3D(third, vertex2, third, vertex3, third, vertex4),
            new Vector3D(third, vertex3, third, vertex4, third, vertex1),
            new Vector3D(third, vertex4, third, vertex1, third, vertex2)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            new Vector3D(1, 2, 4),
            new Vector3D(2, 2, 3),
            new Vector3D(2, 3, 4),
            new Vector3D(1, 3, 3)
        });
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testIsometry
    public void testIsometry() throws MathArithmeticException, MathIllegalArgumentException {
        Vector3D vertex1 = new Vector3D(1.1, 2.2, 3.3);
        Vector3D vertex2 = new Vector3D(2.0, 2.4, 4.2);
        Vector3D vertex3 = new Vector3D(2.8, 3.3, 3.7);
        Vector3D vertex4 = new Vector3D(1.0, 3.6, 4.5);
        @SuppressWarnings("unchecked")
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1),
                new Plane(vertex2, vertex3, vertex4),
                new Plane(vertex4, vertex3, vertex1),
                new Plane(vertex1, vertex2, vertex4));
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Vector3D s = new Vector3D(10.2, 4.3, -6.7);
        Vector3D c = new Vector3D(-0.2, 2.1, -3.2);
        Rotation r = new Rotation(new Vector3D(6.2, -4.4, 2.1), 0.12);

        tree = tree.rotate(c, r).translate(s);

        Vector3D newB =
            new Vector3D(1.0, s,
                         1.0, c,
                         1.0, r.applyTo(barycenter.subtract(c)));
        Assert.assertEquals(0.0,
                            newB.subtract(tree.getBarycenter()).getNorm(),
                            1.0e-10);

        final Vector3D[] expectedV = new Vector3D[] {
            new Vector3D(1.0, s,
                         1.0, c,
                         1.0, r.applyTo(vertex1.subtract(c))),
                         new Vector3D(1.0, s,
                                      1.0, c,
                                      1.0, r.applyTo(vertex2.subtract(c))),
                                      new Vector3D(1.0, s,
                                                   1.0, c,
                                                   1.0, r.applyTo(vertex3.subtract(c))),
                                                   new Vector3D(1.0, s,
                                                                1.0, c,
                                                                1.0, r.applyTo(vertex4.subtract(c)))
        };
        tree.getTree(true).visit(new BSPTreeVisitor<Euclidean3D>() {

            public Order visitOrder(BSPTree<Euclidean3D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            public void visitInternalNode(BSPTree<Euclidean3D> node) {
                @SuppressWarnings("unchecked")
                BoundaryAttribute<Euclidean3D> attribute =
                    (BoundaryAttribute<Euclidean3D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    checkFacet((SubPlane) attribute.getPlusOutside());
                }
                if (attribute.getPlusInside() != null) {
                    checkFacet((SubPlane) attribute.getPlusInside());
                }
            }

            public void visitLeafNode(BSPTree<Euclidean3D> node) {
            }

            private void checkFacet(SubPlane facet) {
                Plane plane = (Plane) facet.getHyperplane();
                Vector2D[][] vertices =
                    ((PolygonsSet) facet.getRemainingRegion()).getVertices();
                Assert.assertEquals(1, vertices.length);
                for (int i = 0; i < vertices[0].length; ++i) {
                    Vector3D v = plane.toSpace(vertices[0][i]);
                    double d = Double.POSITIVE_INFINITY;
                    for (int k = 0; k < expectedV.length; ++k) {
                        d = FastMath.min(d, v.subtract(expectedV[k]).getNorm());
                    }
                    Assert.assertEquals(0, d, 1.0e-10);
                }
            }

        });

    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testBuildBox
    public void testBuildBox() {
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;
        PolyhedronsSet tree =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(x, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(y, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(z, barycenter.getZ(), 1.0e-10);
        Assert.assertEquals(8 * l * w * w, tree.getSize(), 1.0e-10);
        Assert.assertEquals(8 * w * (2 * l + w), tree.getBoundarySize(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testCross
    public void testCross() {

        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;
        PolyhedronsSet xBeam =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w);
        PolyhedronsSet yBeam =
            new PolyhedronsSet(x - w, x + w, y - l, y + l, z - w, z + w);
        PolyhedronsSet zBeam =
            new PolyhedronsSet(x - w, x + w, y - w, y + w, z - l, z + l);
        RegionFactory<Euclidean3D> factory = new RegionFactory<Euclidean3D>();
        PolyhedronsSet tree = (PolyhedronsSet) factory.union(xBeam, factory.union(yBeam, zBeam));
        Vector3D barycenter = (Vector3D) tree.getBarycenter();

        Assert.assertEquals(x, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(y, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(z, barycenter.getZ(), 1.0e-10);
        Assert.assertEquals(8 * w * w * (3 * l - 2 * w), tree.getSize(), 1.0e-10);
        Assert.assertEquals(24 * w * (2 * l - w), tree.getBoundarySize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testIssue780
    public void testIssue780() throws MathArithmeticException {
        float[] coords = {
            1.000000f, -1.000000f, -1.000000f, 
            1.000000f, -1.000000f, 1.000000f, 
            -1.000000f, -1.000000f, 1.000000f, 
            -1.000000f, -1.000000f, -1.000000f, 
            1.000000f, 1.000000f, -1f, 
            0.999999f, 1.000000f, 1.000000f,   
            -1.000000f, 1.000000f, 1.000000f, 
            -1.000000f, 1.000000f, -1.000000f};
        int[] indices = {
            0, 1, 2, 0, 2, 3, 
            4, 7, 6, 4, 6, 5, 
            0, 4, 5, 0, 5, 1, 
            1, 5, 6, 1, 6, 2, 
            2, 6, 7, 2, 7, 3, 
            4, 0, 3, 4, 3, 7};
        ArrayList<SubHyperplane<Euclidean3D>> subHyperplaneList = new ArrayList<SubHyperplane<Euclidean3D>>();
        for (int idx = 0; idx < indices.length; idx += 3) {
            int idxA = indices[idx] * 3;
            int idxB = indices[idx + 1] * 3;
            int idxC = indices[idx + 2] * 3;
            Vector3D v_1 = new Vector3D(coords[idxA], coords[idxA + 1], coords[idxA + 2]);
            Vector3D v_2 = new Vector3D(coords[idxB], coords[idxB + 1], coords[idxB + 2]);
            Vector3D v_3 = new Vector3D(coords[idxC], coords[idxC + 1], coords[idxC + 2]);
            Vector3D[] vertices = {v_1, v_2, v_3};
            Plane polyPlane = new Plane(v_1, v_2, v_3);
            ArrayList<SubHyperplane<Euclidean2D>> lines = new ArrayList<SubHyperplane<Euclidean2D>>();

            Vector2D[] projPts = new Vector2D[vertices.length];
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                projPts[ptIdx] = polyPlane.toSubSpace(vertices[ptIdx]);
            }

            SubLine lineInPlane = null;
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                lineInPlane = new SubLine(projPts[ptIdx], projPts[(ptIdx + 1) % projPts.length]);
                lines.add(lineInPlane);
            }
            Region<Euclidean2D> polyRegion = new PolygonsSet(lines);
            SubPlane polygon = new SubPlane(polyPlane, polyRegion);
            subHyperplaneList.add(polygon);
        }
        PolyhedronsSet polyhedronsSet = new PolyhedronsSet(subHyperplaneList);
        Assert.assertEquals( 8.0, polyhedronsSet.getSize(), 3.0e-6);
        Assert.assertEquals(24.0, polyhedronsSet.getBoundarySize(), 5.0e-6);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testEndPoints
    public void testEndPoints() throws MathIllegalArgumentException {
        Vector3D p1 = new Vector3D(-1, -7, 2);
        Vector3D p2 = new Vector3D(7, -1, 0);
        Segment segment = new Segment(p1, p2, new Line(p1, p2));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector3D(-1, -7, 2).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertEquals(0.0, new Vector3D( 7, -1, 0).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testNoEndPoints
    public void testNoEndPoints() throws MathIllegalArgumentException {
        SubLine wholeLine = new Line(new Vector3D(-1, 7, 2), new Vector3D(7, 1, 0)).wholeLine();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testNoSegments
    public void testNoSegments() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, 0)),
                                    (IntervalsSet) new RegionFactory<Euclidean1D>().getComplement(new IntervalsSet()));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testSeveralSegments
    public void testSeveralSegments() throws MathIllegalArgumentException {
        SubLine twoSubs = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, 0)),
                                      (IntervalsSet) new RegionFactory<Euclidean1D>().union(new IntervalsSet(1, 2),
                                                                                            new IntervalsSet(3, 4)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testHalfInfiniteNeg
    public void testHalfInfiniteNeg() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, -2)),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertEquals(0.0, new Vector3D(3, -4, 0).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testHalfInfinitePos
    public void testHalfInfinitePos() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, -2)),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector3D(3, -4, 0).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideInside
    public void testIntersectionInsideInside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 2, 2));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, false)), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideBoundary
    public void testIntersectionInsideBoundary() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 1, 1));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideOutside
    public void testIntersectionInsideOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionBoundaryBoundary
    public void testIntersectionBoundaryBoundary() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(2, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 1, 1));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionBoundaryOutside
    public void testIntersectionBoundaryOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(2, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionOutsideOutside
    public void testIntersectionOutsideOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(1.5, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionNotIntersecting
    public void testIntersectionNotIntersecting() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(1.5, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 3, 0), new Vector3D(2, 3, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSimplyConnected
    public void testSimplyConnected() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(36.0, 22.0),
                new Vector2D(39.0, 32.0),
                new Vector2D(19.0, 32.0),
                new Vector2D( 6.0, 16.0),
                new Vector2D(31.0, 10.0),
                new Vector2D(42.0, 16.0),
                new Vector2D(34.0, 20.0),
                new Vector2D(29.0, 19.0),
                new Vector2D(23.0, 22.0),
                new Vector2D(33.0, 25.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        Assert.assertEquals(Region.Location.OUTSIDE, set.checkPoint(new Vector2D(50.0, 30.0)));
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(30.0, 15.0),
            new Vector2D(15.0, 20.0),
            new Vector2D(24.0, 25.0),
            new Vector2D(35.0, 30.0),
            new Vector2D(19.0, 17.0)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(50.0, 30.0),
            new Vector2D(30.0, 35.0),
            new Vector2D(10.0, 25.0),
            new Vector2D(10.0, 10.0),
            new Vector2D(40.0, 10.0),
            new Vector2D(50.0, 15.0),
            new Vector2D(30.0, 22.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(30.0, 32.0),
            new Vector2D(34.0, 20.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testStair
    public void testStair() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0, 0.0),
                new Vector2D( 0.0, 2.0),
                new Vector2D(-0.1, 2.0),
                new Vector2D(-0.1, 1.0),
                new Vector2D(-0.3, 1.0),
                new Vector2D(-0.3, 1.5),
                new Vector2D(-1.3, 1.5),
                new Vector2D(-1.3, 2.0),
                new Vector2D(-1.8, 2.0),
                new Vector2D(-1.8 - 1.0 / FastMath.sqrt(2.0),
                            2.0 - 1.0 / FastMath.sqrt(2.0))
            }
        };

        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);

        Assert.assertEquals(1.1 + 0.95 * FastMath.sqrt(2.0), set.getSize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testHole
    public void testHole() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 0.0),
                new Vector2D(3.0, 0.0),
                new Vector2D(3.0, 3.0),
                new Vector2D(0.0, 3.0)
            }, new Vector2D[] {
                new Vector2D(1.0, 2.0),
                new Vector2D(2.0, 2.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(1.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(2.5, 0.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(0.5, 2.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(1.5, 1.5),
            new Vector2D(3.5, 1.0),
            new Vector2D(4.0, 1.5),
            new Vector2D(6.0, 6.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(1.5, 0.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(1.5, 2.0),
            new Vector2D(1.5, 3.0),
            new Vector2D(3.0, 3.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testDisjointPolygons
    public void testDisjointPolygons() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 1.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(1.0, 2.0)
            }, new Vector2D[] {
                new Vector2D(4.0, 0.0),
                new Vector2D(5.0, 1.0),
                new Vector2D(3.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(new Vector2D(1.0, 1.5)));
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 1.5),
            new Vector2D(4.5, 0.8)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 0.0),
            new Vector2D(3.5, 1.2),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 4.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(3.5, 0.5),
            new Vector2D(0.0, 1.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testOppositeHyperplanes
    public void testOppositeHyperplanes() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(1.0, 0.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(3.0, 1.0),
                new Vector2D(2.0, 2.0),
                new Vector2D(1.0, 1.0),
                new Vector2D(0.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSingularPoint
    public void testSingularPoint() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 1.0,  0.0),
                new Vector2D( 1.0,  1.0),
                new Vector2D( 0.0,  1.0),
                new Vector2D( 0.0,  0.0),
                new Vector2D(-1.0,  0.0),
                new Vector2D(-1.0, -1.0),
                new Vector2D( 0.0, -1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testLineIntersection
    public void testLineIntersection() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set = buildSet(vertices);

        Line l1 = new Line(new Vector2D(-1.5, 0.0), FastMath.PI / 4);
        SubLine s1 = (SubLine) set.intersection(l1.wholeHyperplane());
        List<Interval> i1 = ((IntervalsSet) s1.getRemainingRegion()).asList();
        Assert.assertEquals(2, i1.size());
        Interval v10 = i1.get(0);
        Vector2D p10Lower = l1.toSpace(new Vector1D(v10.getInf()));
        Assert.assertEquals(0.0, p10Lower.getX(), 1.0e-10);
        Assert.assertEquals(1.5, p10Lower.getY(), 1.0e-10);
        Vector2D p10Upper = l1.toSpace(new Vector1D(v10.getSup()));
        Assert.assertEquals(0.5, p10Upper.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p10Upper.getY(), 1.0e-10);
        Interval v11 = i1.get(1);
        Vector2D p11Lower = l1.toSpace(new Vector1D(v11.getInf()));
        Assert.assertEquals(1.0, p11Lower.getX(), 1.0e-10);
        Assert.assertEquals(2.5, p11Lower.getY(), 1.0e-10);
        Vector2D p11Upper = l1.toSpace(new Vector1D(v11.getSup()));
        Assert.assertEquals(1.5, p11Upper.getX(), 1.0e-10);
        Assert.assertEquals(3.0, p11Upper.getY(), 1.0e-10);

        Line l2 = new Line(new Vector2D(-1.0, 2.0), 0);
        SubLine s2 = (SubLine) set.intersection(l2.wholeHyperplane());
        List<Interval> i2 = ((IntervalsSet) s2.getRemainingRegion()).asList();
        Assert.assertEquals(1, i2.size());
        Interval v20 = i2.get(0);
        Vector2D p20Lower = l2.toSpace(new Vector1D(v20.getInf()));
        Assert.assertEquals(1.0, p20Lower.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p20Lower.getY(), 1.0e-10);
        Vector2D p20Upper = l2.toSpace(new Vector1D(v20.getSup()));
        Assert.assertEquals(3.0, p20Upper.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p20Upper.getY(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testUnlimitedSubHyperplane
    public void testUnlimitedSubHyperplane() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 0.0),
                new Vector2D(4.0, 0.0),
                new Vector2D(1.4, 1.5),
                new Vector2D(0.0, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(1.4,  0.2),
                new Vector2D(2.8, -1.2),
                new Vector2D(2.5,  0.6)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        PolygonsSet set =
            (PolygonsSet) new RegionFactory<Euclidean2D>().union(set1.copySelf(),
                                                                 set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0,  0.0),
                new Vector2D(1.6,  0.0),
                new Vector2D(2.8, -1.2),
                new Vector2D(2.6,  0.0),
                new Vector2D(4.0,  0.0),
                new Vector2D(1.4,  1.5),
                new Vector2D(0.0,  3.5)
            }
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testUnion
    public void testUnion() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().union(set1.copySelf(),
                                                                                set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(0.5, 0.5),
            new Vector2D(2.0, 2.0),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 2.5)
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIntersection
    public void testIntersection() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().intersection(set1.copySelf(),
                                                                                       set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 1.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.5, 1.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 1.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(0.5, 0.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(2.0, 2.0),
            new Vector2D(1.0, 1.5),
            new Vector2D(1.5, 2.0)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testXor
    public void testXor() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().xor(set1.copySelf(),
                                                                              set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            },
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 2.0,  1.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 1.5, 1.5),
            new Vector2D( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(2.0, 2.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(2.0, 1.5),
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 2.5)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testDifference
    public void testDifference() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().difference(set1.copySelf(),
                                                                                     set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 1.0,  1.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 0.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D( 2.5, 2.5),
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 1.5, 1.5),
            new Vector2D( 3.5, 2.5),
            new Vector2D( 1.5, 2.5),
            new Vector2D( 2.5, 1.5),
            new Vector2D( 2.0, 1.5),
            new Vector2D( 2.0, 2.0),
            new Vector2D( 2.5, 1.0),
            new Vector2D( 2.5, 2.5),
            new Vector2D( 3.0, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testEmptyDifference
    public void testEmptyDifference() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.5, 3.5),
                new Vector2D( 0.5, 4.5),
                new Vector2D(-0.5, 4.5),
                new Vector2D(-0.5, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0, 2.0),
                new Vector2D( 1.0, 8.0),
                new Vector2D(-1.0, 8.0),
                new Vector2D(-1.0, 2.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        Assert.assertTrue(new RegionFactory<Euclidean2D>().difference(set1.copySelf(), set2.copySelf()).isEmpty());
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testChoppedHexagon
    public void testChoppedHexagon() {
        double pi6   = FastMath.PI / 6.0;
        double sqrt3 = FastMath.sqrt(3.0);
        SubLine[] hyp = {
            new Line(new Vector2D(   0.0, 1.0),  5 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 1.0),  7 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 1.0),  9 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 0.0), 11 * pi6).wholeHyperplane(),
            new Line(new Vector2D(   0.0, 0.0), 13 * pi6).wholeHyperplane(),
            new Line(new Vector2D(   0.0, 1.0),  3 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-5.0 * sqrt3 / 6.0, 0.0), 9 * pi6).wholeHyperplane()
        };
        hyp[1] = (SubLine) hyp[1].split(hyp[0].getHyperplane()).getMinus();
        hyp[2] = (SubLine) hyp[2].split(hyp[1].getHyperplane()).getMinus();
        hyp[3] = (SubLine) hyp[3].split(hyp[2].getHyperplane()).getMinus();
        hyp[4] = (SubLine) hyp[4].split(hyp[3].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[5] = (SubLine) hyp[5].split(hyp[4].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[6] = (SubLine) hyp[6].split(hyp[3].getHyperplane()).getMinus().split(hyp[1].getHyperplane()).getMinus();
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(Boolean.TRUE);
        for (int i = hyp.length - 1; i >= 0; --i) {
            tree = new BSPTree<Euclidean2D>(hyp[i], new BSPTree<Euclidean2D>(Boolean.FALSE), tree, null);
        }
        PolygonsSet set = new PolygonsSet(tree);
        SubLine splitter =
            new Line(new Vector2D(-2.0 * sqrt3 / 3.0, 0.0), 9 * pi6).wholeHyperplane();
        PolygonsSet slice =
            new PolygonsSet(new BSPTree<Euclidean2D>(splitter,
                                                     set.getTree(false).split(splitter).getPlus(),
                                                     new BSPTree<Euclidean2D>(Boolean.FALSE), null));
        Assert.assertEquals(Region.Location.OUTSIDE,
                            slice.checkPoint(new Vector2D(0.1, 0.5)));
        Assert.assertEquals(11.0 / 3.0, slice.getBoundarySize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testConcentric
    public void testConcentric() {
        double h = FastMath.sqrt(3.0) / 2.0;
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.00, 0.1 * h),
                new Vector2D( 0.05, 0.1 * h),
                new Vector2D( 0.10, 0.2 * h),
                new Vector2D( 0.05, 0.3 * h),
                new Vector2D(-0.05, 0.3 * h),
                new Vector2D(-0.10, 0.2 * h),
                new Vector2D(-0.05, 0.1 * h)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.00, 0.0 * h),
                new Vector2D( 0.10, 0.0 * h),
                new Vector2D( 0.20, 0.2 * h),
                new Vector2D( 0.10, 0.4 * h),
                new Vector2D(-0.10, 0.4 * h),
                new Vector2D(-0.20, 0.2 * h),
                new Vector2D(-0.10, 0.0 * h)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        Assert.assertTrue(set2.contains(set1));
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testBug20040520
    public void testBug20040520() {
        BSPTree<Euclidean2D> a0 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.05),
                                                  new Vector2D(0.90, -0.10)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a1 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.10),
                                                  new Vector2D(0.90, -0.10)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), a0, null);
        BSPTree<Euclidean2D> a2 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.90, -0.05),
                                                  new Vector2D(0.85, -0.05)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), a1, null);
        BSPTree<Euclidean2D> a3 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.82, -0.05),
                                                  new Vector2D(0.82, -0.08)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a4 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.80, -0.05),
                                                   false),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), a3, null);
        BSPTree<Euclidean2D> a5 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.82, -0.08),
                                                  new Vector2D(0.82, -0.18)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a6 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.82, -0.18),
                                                   new Vector2D(0.85, -0.15),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), a5, null);
        BSPTree<Euclidean2D> a7 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.82, -0.08),
                                                   false),
                                                   a4, a6, null);
        BSPTree<Euclidean2D> a8 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.85, -0.25),
                                               new Vector2D(0.85,  0.05)),
                                               a2, a7, null);
        BSPTree<Euclidean2D> a9 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.90,  0.05),
                                               new Vector2D(0.90, -0.50)),
                                               a8, new BSPTree<Euclidean2D>(Boolean.FALSE), null);

        BSPTree<Euclidean2D> b0 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.92, -0.12),
                                                  new Vector2D(0.92, -0.08)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> b1 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.92, -0.08),
                                                   new Vector2D(0.90, -0.10),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), b0, null);
        BSPTree<Euclidean2D> b2 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.92, -0.18),
                                                  new Vector2D(0.92, -0.12)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> b3 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.15),
                                                  new Vector2D(0.90, -0.20)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), b2, null);
        BSPTree<Euclidean2D> b4 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.95, -0.15),
                                                  new Vector2D(0.85, -0.05)),
                                                  b1, b3, null);
        BSPTree<Euclidean2D> b5 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.85, -0.25),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), b4, null);
        BSPTree<Euclidean2D> b6 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.0, -1.10),
                                               new Vector2D(1.0, -0.10)),
                                               new BSPTree<Euclidean2D>(Boolean.FALSE), b5, null);

        PolygonsSet c =
            (PolygonsSet) new RegionFactory<Euclidean2D>().union(new PolygonsSet(a9),
                                                                 new PolygonsSet(b6));

        checkPoints(Region.Location.INSIDE, c, new Vector2D[] {
            new Vector2D(0.83, -0.06),
            new Vector2D(0.83, -0.15),
            new Vector2D(0.88, -0.15),
            new Vector2D(0.88, -0.09),
            new Vector2D(0.88, -0.07),
            new Vector2D(0.91, -0.18),
            new Vector2D(0.91, -0.10)
        });

        checkPoints(Region.Location.OUTSIDE, c, new Vector2D[] {
            new Vector2D(0.80, -0.10),
            new Vector2D(0.83, -0.50),
            new Vector2D(0.83, -0.20),
            new Vector2D(0.83, -0.02),
            new Vector2D(0.87, -0.50),
            new Vector2D(0.87, -0.20),
            new Vector2D(0.87, -0.02),
            new Vector2D(0.91, -0.20),
            new Vector2D(0.91, -0.08),
            new Vector2D(0.93, -0.15)
        });

        checkVertices(c.getVertices(),
                      new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.85, -0.15),
                new Vector2D(0.90, -0.20),
                new Vector2D(0.92, -0.18),
                new Vector2D(0.92, -0.08),
                new Vector2D(0.90, -0.10),
                new Vector2D(0.90, -0.05),
                new Vector2D(0.82, -0.05),
                new Vector2D(0.82, -0.18),
            }
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testBug20041003
    public void testBug20041003() {

        Line[] l = {
            new Line(new Vector2D(0.0, 0.625000007541172),
                     new Vector2D(1.0, 0.625000007541172)),
                     new Line(new Vector2D(-0.19204433621902645, 0.0),
                              new Vector2D(-0.19204433621902645, 1.0)),
                              new Line(new Vector2D(-0.40303524786887,  0.4248364535319128),
                                       new Vector2D(-1.12851149797877, -0.2634107480798909)),
                                       new Line(new Vector2D(0.0, 2.0),
                                                new Vector2D(1.0, 2.0))
        };

        BSPTree<Euclidean2D> node1 =
            new BSPTree<Euclidean2D>(new SubLine(l[0],
                                          new IntervalsSet(intersectionAbscissa(l[0], l[1]),
                                                           intersectionAbscissa(l[0], l[2]))),
                                                           new BSPTree<Euclidean2D>(Boolean.TRUE), new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                           null);
        BSPTree<Euclidean2D> node2 =
            new BSPTree<Euclidean2D>(new SubLine(l[1],
                                          new IntervalsSet(intersectionAbscissa(l[1], l[2]),
                                                           intersectionAbscissa(l[1], l[3]))),
                                                           node1, new BSPTree<Euclidean2D>(Boolean.FALSE), null);
        BSPTree<Euclidean2D> node3 =
            new BSPTree<Euclidean2D>(new SubLine(l[2],
                                          new IntervalsSet(intersectionAbscissa(l[2], l[3]),
                                                           Double.POSITIVE_INFINITY)),
                                                           node2, new BSPTree<Euclidean2D>(Boolean.FALSE), null);
        BSPTree<Euclidean2D> node4 =
            new BSPTree<Euclidean2D>(l[3].wholeHyperplane(), node3, new BSPTree<Euclidean2D>(Boolean.FALSE), null);

        PolygonsSet set = new PolygonsSet(node4);
        Assert.assertEquals(0, set.getVertices().length);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSqueezedHexa
    public void testSqueezedHexa() {
        PolygonsSet set = new PolygonsSet(1.0e-10,
                                          new Vector2D(-6, -4), new Vector2D(-8, -8), new Vector2D(  8, -8),
                                          new Vector2D( 6, -4), new Vector2D(10,  4), new Vector2D(-10,  4));
        Assert.assertEquals(Location.OUTSIDE, set.checkPoint(new Vector2D(0, 6)));
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIssue880Simplified
    public void testIssue880Simplified() {

        Vector2D[] vertices1 = new Vector2D[] {
            new Vector2D( 90.13595870833188,  38.33604606376991),
            new Vector2D( 90.14047850603913,  38.34600084496253),
            new Vector2D( 90.11045289492762,  38.36801537312368),
            new Vector2D( 90.10871471476526,  38.36878044144294),
            new Vector2D( 90.10424901707671,  38.374300101757),
            new Vector2D( 90.0979455456843,   38.373578376172475),
            new Vector2D( 90.09081227075944,  38.37526295920463),
            new Vector2D( 90.09081378927135,  38.375193883266434)
        };
        PolygonsSet set1 = new PolygonsSet(1.0e-10, vertices1);
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.12,  38.32)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.135, 38.355)));

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIssue880Complete
    public void testIssue880Complete() {
        Vector2D[] vertices1 = new Vector2D[] {
                new Vector2D( 90.08714908223715,  38.370299337260235),
                new Vector2D( 90.08709517675004,  38.3702895991413),
                new Vector2D( 90.08401538704919,  38.368849330127944),
                new Vector2D( 90.08258210430711,  38.367634558585564),
                new Vector2D( 90.08251455106665,  38.36763409247078),
                new Vector2D( 90.08106599752608,  38.36761621664249),
                new Vector2D( 90.08249585300035,  38.36753627557965),
                new Vector2D( 90.09075743352184,  38.35914647644972),
                new Vector2D( 90.09099945896571,  38.35896264724079),
                new Vector2D( 90.09269383800086,  38.34595756121246),
                new Vector2D( 90.09638631543191,  38.3457988093121),
                new Vector2D( 90.09666417351019,  38.34523360999418),
                new Vector2D( 90.1297082145872,  38.337670454923625),
                new Vector2D( 90.12971687748956,  38.337669827794684),
                new Vector2D( 90.1240820219179,  38.34328502001131),
                new Vector2D( 90.13084259656404,  38.34017811765017),
                new Vector2D( 90.13378567942857,  38.33860579180606),
                new Vector2D( 90.13519557833206,  38.33621054663689),
                new Vector2D( 90.13545616732307,  38.33614965452864),
                new Vector2D( 90.13553111202748,  38.33613962818305),
                new Vector2D( 90.1356903436448,  38.33610227127048),
                new Vector2D( 90.13576283227428,  38.33609255422783),
                new Vector2D( 90.13595870833188,  38.33604606376991),
                new Vector2D( 90.1361556630693,  38.3360024198866),
                new Vector2D( 90.13622408795709,  38.335987048115726),
                new Vector2D( 90.13696189099994,  38.33581914328681),
                new Vector2D( 90.13746655304897,  38.33616706665265),
                new Vector2D( 90.13845973716064,  38.33650776167099),
                new Vector2D( 90.13950901827667,  38.3368469456463),
                new Vector2D( 90.14393814424852,  38.337591835857495),
                new Vector2D( 90.14483839716831,  38.337076122362475),
                new Vector2D( 90.14565474433601,  38.33769000964429),
                new Vector2D( 90.14569421179482,  38.3377117256905),
                new Vector2D( 90.14577067124333,  38.33770883625908),
                new Vector2D( 90.14600350631684,  38.337714326520995),
                new Vector2D( 90.14600355139731,  38.33771435193319),
                new Vector2D( 90.14600369112401,  38.33771443882085),
                new Vector2D( 90.14600382486884,  38.33771453466096),
                new Vector2D( 90.14600395205912,  38.33771463904344),
                new Vector2D( 90.14600407214999,  38.337714751520764),
                new Vector2D( 90.14600418462749,  38.337714871611695),
                new Vector2D( 90.14600422249327,  38.337714915811034),
                new Vector2D( 90.14867838361471,  38.34113888210675),
                new Vector2D( 90.14923750157374,  38.341582537502575),
                new Vector2D( 90.14877083250991,  38.34160685841391),
                new Vector2D( 90.14816667319519,  38.34244232585684),
                new Vector2D( 90.14797696744586,  38.34248455284745),
                new Vector2D( 90.14484318014337,  38.34385573215269),
                new Vector2D( 90.14477919958296,  38.3453797747614),
                new Vector2D( 90.14202393306448,  38.34464324839456),
                new Vector2D( 90.14198920640195,  38.344651155237216),
                new Vector2D( 90.14155207025175,  38.34486424263724),
                new Vector2D( 90.1415196143314,  38.344871730519),
                new Vector2D( 90.14128611910814,  38.34500196593859),
                new Vector2D( 90.14047850603913,  38.34600084496253),
                new Vector2D( 90.14045907000337,  38.34601860032171),
                new Vector2D( 90.14039496493928,  38.346223030432384),
                new Vector2D( 90.14037626063737,  38.346240203360026),
                new Vector2D( 90.14030005823724,  38.34646920000705),
                new Vector2D( 90.13799164754806,  38.34903093011013),
                new Vector2D( 90.11045289492762,  38.36801537312368),
                new Vector2D( 90.10871471476526,  38.36878044144294),
                new Vector2D( 90.10424901707671,  38.374300101757),
                new Vector2D( 90.10263482039932,  38.37310041316073),
                new Vector2D( 90.09834601753448,  38.373615053823414),
                new Vector2D( 90.0979455456843,  38.373578376172475),
                new Vector2D( 90.09086514328669,  38.37527884194668),
                new Vector2D( 90.09084931407364,  38.37590801712463),
                new Vector2D( 90.09081227075944,  38.37526295920463),
                new Vector2D( 90.09081378927135,  38.375193883266434)
        };
        PolygonsSet set1 = new PolygonsSet(1.0e-8, vertices1);
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0905,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.09084, 38.3755)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0913,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.1042,  38.3739)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.1111,  38.3673)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0959,  38.3457)));

        Vector2D[] vertices2 = new Vector2D[] {
                new Vector2D( 90.13067558880044,  38.36977255037573),
                new Vector2D( 90.12907570488,  38.36817308242706),
                new Vector2D( 90.1342774136516,  38.356886880294724),
                new Vector2D( 90.13090330629757,  38.34664392676211),
                new Vector2D( 90.13078571364593,  38.344904617518466),
                new Vector2D( 90.1315602208914,  38.3447185040846),
                new Vector2D( 90.1316336226821,  38.34470643148342),
                new Vector2D( 90.134020944832,  38.340936644972885),
                new Vector2D( 90.13912536387306,  38.335497255122334),
                new Vector2D( 90.1396178806582,  38.334878075552126),
                new Vector2D( 90.14083049696671,  38.33316530644106),
                new Vector2D( 90.14145252901329,  38.33152722916191),
                new Vector2D( 90.1404779335565,  38.32863516047786),
                new Vector2D( 90.14282712131586,  38.327504432532066),
                new Vector2D( 90.14616669875488,  38.3237354115015),
                new Vector2D( 90.14860976050608,  38.315714862457924),
                new Vector2D( 90.14999277782437,  38.3164932507504),
                new Vector2D( 90.15005207194997,  38.316534677663356),
                new Vector2D( 90.15508513859612,  38.31878731691609),
                new Vector2D( 90.15919938519221,  38.31852743183782),
                new Vector2D( 90.16093758658837,  38.31880662005153),
                new Vector2D( 90.16099420184912,  38.318825953291594),
                new Vector2D( 90.1665411125756,  38.31859497874757),
                new Vector2D( 90.16999653861313,  38.32505772048029),
                new Vector2D( 90.17475243391698,  38.32594398441148),
                new Vector2D( 90.17940844844992,  38.327427213761325),
                new Vector2D( 90.20951909541378,  38.330616833491774),
                new Vector2D( 90.2155400467941,  38.331746223670336),
                new Vector2D( 90.21559881391778,  38.33175551425302),
                new Vector2D( 90.21916646426041,  38.332584299620805),
                new Vector2D( 90.23863749852285,  38.34778978875795),
                new Vector2D( 90.25459855175802,  38.357790570608984),
                new Vector2D( 90.25964298227257,  38.356918010203174),
                new Vector2D( 90.26024593994703,  38.361692743151366),
                new Vector2D( 90.26146187570015,  38.36311080550837),
                new Vector2D( 90.26614159359622,  38.36510808579902),
                new Vector2D( 90.26621342936448,  38.36507942500333),
                new Vector2D( 90.26652190211962,  38.36494042196722),
                new Vector2D( 90.26621240678867,  38.365113172030874),
                new Vector2D( 90.26614057102057,  38.365141832826794),
                new Vector2D( 90.26380080055299,  38.3660381760273),
                new Vector2D( 90.26315345241,  38.36670658276421),
                new Vector2D( 90.26251574942881,  38.367490323488084),
                new Vector2D( 90.26247873448426,  38.36755266444749),
                new Vector2D( 90.26234628016698,  38.36787989125406),
                new Vector2D( 90.26214559424784,  38.36945909356126),
                new Vector2D( 90.25861728442555,  38.37200753430875),
                new Vector2D( 90.23905557537864,  38.375405314295904),
                new Vector2D( 90.22517251874075,  38.38984691662256),
                new Vector2D( 90.22549955153215,  38.3911564273979),
                new Vector2D( 90.22434386063355,  38.391476432092134),
                new Vector2D( 90.22147729457276,  38.39134652252034),
                new Vector2D( 90.22142070120117,  38.391349167741964),
                new Vector2D( 90.20665060751588,  38.39475580900313),
                new Vector2D( 90.20042268367109,  38.39842558622888),
                new Vector2D( 90.17423771242085,  38.402727751805344),
                new Vector2D( 90.16756796257476,  38.40913898597597),
                new Vector2D( 90.16728283954308,  38.411255399912875),
                new Vector2D( 90.16703538220418,  38.41136059866693),
                new Vector2D( 90.16725865657685,  38.41013618805954),
                new Vector2D( 90.16746107640665,  38.40902614307544),
                new Vector2D( 90.16122795307462,  38.39773101873203)
        };
        PolygonsSet set2 = new PolygonsSet(1.0e-8, vertices2);
        PolygonsSet set  = (PolygonsSet) new
                RegionFactory<Euclidean2D>().difference(set1.copySelf(),
                                                        set2.copySelf());

        Vector2D[][] verticies = set.getVertices();
        Assert.assertTrue(verticies[0][0] != null);
        Assert.assertEquals(1, verticies.length);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testEndPoints
    public void testEndPoints() {
        Vector2D p1 = new Vector2D(-1, -7);
        Vector2D p2 = new Vector2D(7, -1);
        Segment segment = new Segment(p1, p2, new Line(p1, p2));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(-1, -7).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertEquals(0.0, new Vector2D( 7, -1).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testNoEndPoints
    public void testNoEndPoints() {
        SubLine wholeLine = new Line(new Vector2D(-1, 7), new Vector2D(7, 1)).wholeHyperplane();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testNoSegments
    public void testNoSegments() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().getComplement(new IntervalsSet()));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testSeveralSegments
    public void testSeveralSegments() {
        SubLine twoSubs = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().union(new IntervalsSet(1, 2),
                                                                           new IntervalsSet(3, 4)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testHalfInfiniteNeg
    public void testHalfInfiniteNeg() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testHalfInfinitePos
    public void testHalfInfinitePos() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideInside
    public void testIntersectionInsideInside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 2));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, false)), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideBoundary
    public void testIntersectionInsideBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideOutside
    public void testIntersectionInsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionBoundaryBoundary
    public void testIntersectionBoundaryBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionBoundaryOutside
    public void testIntersectionBoundaryOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionOutsideOutside
    public void testIntersectionOutsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(1.5, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionParallel
    public void testIntersectionParallel() {
        final SubLine sub1 = new SubLine(new Vector2D(0, 1), new Vector2D(0, 2));
        final SubLine sub2 = new SubLine(new Vector2D(66, 3), new Vector2D(66, 4));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }
