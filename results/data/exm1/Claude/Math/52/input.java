// buggy code
  public Rotation(Vector3D u1, Vector3D u2, Vector3D v1, Vector3D v2) {

  // norms computation
  double u1u1 = u1.getNormSq();
  double u2u2 = u2.getNormSq();
  double v1v1 = v1.getNormSq();
  double v2v2 = v2.getNormSq();
  if ((u1u1 == 0) || (u2u2 == 0) || (v1v1 == 0) || (v2v2 == 0)) {
    throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_DEFINING_VECTOR);
  }

  // normalize v1 in order to have (v1'|v1') = (u1|u1)
  v1 = new Vector3D(FastMath.sqrt(u1u1 / v1v1), v1);

  // adjust v2 in order to have (u1|u2) = (v1'|v2') and (v2'|v2') = (u2|u2)
  double u1u2   = u1.dotProduct(u2);
  double v1v2   = v1.dotProduct(v2);
  double coeffU = u1u2 / u1u1;
  double coeffV = v1v2 / u1u1;
  double beta   = FastMath.sqrt((u2u2 - u1u2 * coeffU) / (v2v2 - v1v2 * coeffV));
  double alpha  = coeffU - beta * coeffV;
  v2 = new Vector3D(alpha, v1, beta, v2);

  // preliminary computation
  Vector3D uRef  = u1;
  Vector3D vRef  = v1;
  Vector3D v1Su1 = v1.subtract(u1);
  Vector3D v2Su2 = v2.subtract(u2);
  Vector3D k     = v1Su1.crossProduct(v2Su2);
  Vector3D u3    = u1.crossProduct(u2);
  double c       = k.dotProduct(u3);
  if (c == 0) {
    // the (q1, q2, q3) vector is close to the (u1, u2) plane
    // we try other vectors
    Vector3D v3 = Vector3D.crossProduct(v1, v2);
    Vector3D v3Su3 = v3.subtract(u3);
    k = v1Su1.crossProduct(v3Su3);
    Vector3D u2Prime = u1.crossProduct(u3);
    c = k.dotProduct(u2Prime);

    if (c == 0) {
      // the (q1, q2, q3) vector is also close to the (u1, u3) plane,
      // it is almost aligned with u1: we try (u2, u3) and (v2, v3)
      k = v2Su2.crossProduct(v3Su3);;
      c = k.dotProduct(u2.crossProduct(u3));;

      if (c == 0) {
        // the (q1, q2, q3) vector is aligned with everything
        // this is really the identity rotation
        q0 = 1.0;
        q1 = 0.0;
        q2 = 0.0;
        q3 = 0.0;
        return;
      }

      // we will have to use u2 and v2 to compute the scalar part
      uRef = u2;
      vRef = v2;

    }

  }

  // compute the vectorial part
  c = FastMath.sqrt(c);
  double inv = 1.0 / (c + c);
  q1 = inv * k.getX();
  q2 = inv * k.getY();
  q3 = inv * k.getZ();

  // compute the scalar part
   k = new Vector3D(uRef.getY() * q3 - uRef.getZ() * q2,
                    uRef.getZ() * q1 - uRef.getX() * q3,
                    uRef.getX() * q2 - uRef.getY() * q1);
  q0 = vRef.dotProduct(k) / (2 * k.getNormSq());

  }

// relevant test
// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testContains
    public void testContains() {
        Plane p = new Plane(new Vector3D(0, 0, 1), new Vector3D(0, 0, 1));
        Assert.assertTrue(p.contains(new Vector3D(0, 0, 1)));
        Assert.assertTrue(p.contains(new Vector3D(17, -32, 1)));
        Assert.assertTrue(! p.contains(new Vector3D(17, -32, 1.001)));
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testOffset
    public void testOffset() {
        Vector3D p1 = new Vector3D(1, 1, 1);
        Plane p = new Plane(p1, new Vector3D(0.2, 0, 0));
        Assert.assertEquals(-5.0, p.getOffset(new Vector3D(-4, 0, 0)), 1.0e-10);
        Assert.assertEquals(+5.0, p.getOffset(new Vector3D(6, 10, -12)), 1.0e-10);
        Assert.assertEquals(0.3,
                            p.getOffset(new Vector3D(1.0, p1, 0.3, p.getNormal())),
                            1.0e-10);
        Assert.assertEquals(-0.3,
                            p.getOffset(new Vector3D(1.0, p1, -0.3, p.getNormal())),
                            1.0e-10);
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testPoint
    public void testPoint() {
        Plane p = new Plane(new Vector3D(2, -3, 1), new Vector3D(1, 4, 9));
        Assert.assertTrue(p.contains(p.getOrigin()));
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testThreePoints
    public void testThreePoints() {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testRotate
    public void testRotate() {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);
        Vector3D oldNormal = p.getNormal();

        p = p.rotate(p2, new Rotation(p2.subtract(p1), 1.7));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p2, new Rotation(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p1, new Rotation(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testTranslate
    public void testTranslate() {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);

        p = p.translate(new Vector3D(2.0, p.getU(), -1.5, p.getV()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

        p = p.translate(new Vector3D(-1.2, p.getNormal()));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.translate(new Vector3D(+1.2, p.getNormal()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testIntersection
    public void testIntersection() {
        Plane p = new Plane(new Vector3D(1, 2, 3), new Vector3D(-4, 1, -5));
        Line  l = new Line(new Vector3D(0.2, -3.5, 0.7), new Vector3D(1, 1, -1));
        Vector3D point = p.intersection(l);
        Assert.assertTrue(p.contains(point));
        Assert.assertTrue(l.contains(point));
        Assert.assertNull(p.intersection(new Line(new Vector3D(10, 10, 10),
                                                  p.getNormal().orthogonal())));
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testIntersection2
    public void testIntersection2() {
        Vector3D p1  = new Vector3D (1.2, 3.4, -5.8);
        Vector3D p2  = new Vector3D (3.4, -5.8, 1.2);
        Plane    pA  = new Plane(p1, p2, new Vector3D (-2.0, 4.3, 0.7));
        Plane    pB  = new Plane(p1, new Vector3D (11.4, -3.8, 5.1), p2);
        Line     l   = pA.intersection(pB);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(p2));
        Assert.assertNull(pA.intersection(pA));
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testIntersection3
    public void testIntersection3() {
        Vector3D reference = new Vector3D (1.2, 3.4, -5.8);
        Plane p1 = new Plane(reference, new Vector3D(1, 3, 3));
        Plane p2 = new Plane(reference, new Vector3D(-2, 4, 0));
        Plane p3 = new Plane(reference, new Vector3D(7, 0, -4));
        Vector3D p = Plane.intersection(p1, p2, p3);
        Assert.assertEquals(reference.getX(), p.getX(), 1.0e-10);
        Assert.assertEquals(reference.getY(), p.getY(), 1.0e-10);
        Assert.assertEquals(reference.getZ(), p.getZ(), 1.0e-10);
    }

// org.apache.commons.math.geometry.euclidean.threed.PlaneTest::testSimilar
    public void testSimilar() {
        Vector3D p1  = new Vector3D (1.2, 3.4, -5.8);
        Vector3D p2  = new Vector3D (3.4, -5.8, 1.2);
        Vector3D p3  = new Vector3D (-2.0, 4.3, 0.7);
        Plane    pA  = new Plane(p1, p2, p3);
        Plane    pB  = new Plane(p1, new Vector3D (11.4, -3.8, 5.1), p2);
        Assert.assertTrue(! pA.isSimilarTo(pB));
        Assert.assertTrue(pA.isSimilarTo(pA));
        Assert.assertTrue(pA.isSimilarTo(new Plane(p1, p3, p2)));
        Vector3D shift = new Vector3D(0.3, pA.getNormal());
        Assert.assertTrue(! pA.isSimilarTo(new Plane(p1.add(shift),
                                                     p3.add(shift),
                                                     p2.add(shift))));
    }

// org.apache.commons.math.geometry.euclidean.threed.PolyhedronsSetTest::testBox
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

// org.apache.commons.math.geometry.euclidean.threed.PolyhedronsSetTest::testTetrahedron
    public void testTetrahedron() {
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

// org.apache.commons.math.geometry.euclidean.threed.PolyhedronsSetTest::testIsometry
    public void testIsometry() {
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

// org.apache.commons.math.geometry.euclidean.threed.PolyhedronsSetTest::testBuildBox
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

// org.apache.commons.math.geometry.euclidean.threed.PolyhedronsSetTest::testCross
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

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testIdentity
  public void testIdentity() {

    Rotation r = Rotation.IDENTITY;
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(-1, 0, 0, 0, false);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(42, 0, 0, 0, true);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testAxisAngle
  public void testAxisAngle() {

    Rotation r = new Rotation(new Vector3D(10, 10, 10), 2 * FastMath.PI / 3);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_I);
    double s = 1 / FastMath.sqrt(3);
    checkVector(r.getAxis(), new Vector3D(s, s, s));
    checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

    try {
      new Rotation(new Vector3D(0, 0, 0), 2 * FastMath.PI / 3);
      Assert.fail("an exception should have been thrown");
    } catch (ArithmeticException e) {
    }

    r = new Rotation(Vector3D.PLUS_K, 1.5 * FastMath.PI);
    checkVector(r.getAxis(), new Vector3D(0, 0, -1));
    checkAngle(r.getAngle(), 0.5 * FastMath.PI);

    r = new Rotation(Vector3D.PLUS_J, FastMath.PI);
    checkVector(r.getAxis(), Vector3D.PLUS_J);
    checkAngle(r.getAngle(), FastMath.PI);

    checkVector(Rotation.IDENTITY.getAxis(), Vector3D.PLUS_I);

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testRevert
  public void testRevert() {
    Rotation r = new Rotation(0.001, 0.36, 0.48, 0.8, true);
    Rotation reverted = r.revert();
    checkRotation(r.applyTo(reverted), 1, 0, 0, 0);
    checkRotation(reverted.applyTo(r), 1, 0, 0, 0);
    Assert.assertEquals(r.getAngle(), reverted.getAngle(), 1.0e-12);
    Assert.assertEquals(-1, Vector3D.dotProduct(r.getAxis(), reverted.getAxis()), 1.0e-12);
  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testVectorOnePair
  public void testVectorOnePair() {

    Vector3D u = new Vector3D(3, 2, 1);
    Vector3D v = new Vector3D(-4, 2, 2);
    Rotation r = new Rotation(u, v);
    checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

    checkAngle(new Rotation(u, u.negate()).getAngle(), FastMath.PI);

    try {
        new Rotation(u, Vector3D.ZERO);
        Assert.fail("an exception should have been thrown");
    } catch (IllegalArgumentException e) {
        
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testVectorTwoPairs
  public void testVectorTwoPairs() {

    Vector3D u1 = new Vector3D(3, 0, 0);
    Vector3D u2 = new Vector3D(0, 5, 0);
    Vector3D v1 = new Vector3D(0, 0, 2);
    Vector3D v2 = new Vector3D(-2, 0, 2);
    Rotation r = new Rotation(u1, u2, v1, v2);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.MINUS_I);

    r = new Rotation(u1, u2, u1.negate(), u2.negate());
    Vector3D axis = r.getAxis();
    if (Vector3D.dotProduct(axis, Vector3D.PLUS_K) > 0) {
      checkVector(axis, Vector3D.PLUS_K);
    } else {
      checkVector(axis, Vector3D.MINUS_K);
    }
    checkAngle(r.getAngle(), FastMath.PI);

    double sqrt = FastMath.sqrt(2) / 2;
    r = new Rotation(Vector3D.PLUS_I,  Vector3D.PLUS_J,
                     new Vector3D(0.5, 0.5,  sqrt),
                     new Vector3D(0.5, 0.5, -sqrt));
    checkRotation(r, sqrt, 0.5, 0.5, 0);

    r = new Rotation(u1, u2, u1, Vector3D.crossProduct(u1, u2));
    checkRotation(r, sqrt, -sqrt, 0, 0);

    checkRotation(new Rotation(u1, u2, u1, u2), 1, 0, 0, 0);

    try {
        new Rotation(u1, u2, Vector3D.ZERO, v2);
        Assert.fail("an exception should have been thrown");
    } catch (IllegalArgumentException e) {
      
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testMatrix
  public void testMatrix()
    throws NotARotationMatrixException {

    try {
      new Rotation(new double[][] {
                     { 0.0, 1.0, 0.0 },
                     { 1.0, 0.0, 0.0 }
                   }, 1.0e-7);
      Assert.fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
      new Rotation(new double[][] {
                     {  0.445888,  0.797184, -0.407040 },
                     {  0.821760, -0.184320,  0.539200 },
                     { -0.354816,  0.574912,  0.737280 }
                   }, 1.0e-7);
      Assert.fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
        new Rotation(new double[][] {
                       {  0.4,  0.8, -0.4 },
                       { -0.4,  0.6,  0.7 },
                       {  0.8, -0.2,  0.5 }
                     }, 1.0e-15);
        Assert.fail("Expecting NotARotationMatrixException");
      } catch (NotARotationMatrixException nrme) {
        
      }

    checkRotation(new Rotation(new double[][] {
                                 {  0.445888,  0.797184, -0.407040 },
                                 { -0.354816,  0.574912,  0.737280 },
                                 {  0.821760, -0.184320,  0.539200 }
                               }, 1.0e-10),
                  0.8, 0.288, 0.384, 0.36);

    checkRotation(new Rotation(new double[][] {
                                 {  0.539200,  0.737280,  0.407040 },
                                 {  0.184320, -0.574912,  0.797184 },
                                 {  0.821760, -0.354816, -0.445888 }
                              }, 1.0e-10),
                  0.36, 0.8, 0.288, 0.384);

    checkRotation(new Rotation(new double[][] {
                                 { -0.445888,  0.797184, -0.407040 },
                                 {  0.354816,  0.574912,  0.737280 },
                                 {  0.821760,  0.184320, -0.539200 }
                               }, 1.0e-10),
                  0.384, 0.36, 0.8, 0.288);

    checkRotation(new Rotation(new double[][] {
                                 { -0.539200,  0.737280,  0.407040 },
                                 { -0.184320, -0.574912,  0.797184 },
                                 {  0.821760,  0.354816,  0.445888 }
                               }, 1.0e-10),
                  0.288, 0.384, 0.36, 0.8);

    double[][] m1 = { { 0.0, 1.0, 0.0 },
                      { 0.0, 0.0, 1.0 },
                      { 1.0, 0.0, 0.0 } };
    Rotation r = new Rotation(m1, 1.0e-7);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_J);

    double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
                      { 0.48293,  0.78164, -0.39474 },
                      { 0.27296,  0.29396,  0.91602 } };
    r = new Rotation(m2, 1.0e-12);

    double[][] m3 = r.getMatrix();
    double d00 = m2[0][0] - m3[0][0];
    double d01 = m2[0][1] - m3[0][1];
    double d02 = m2[0][2] - m3[0][2];
    double d10 = m2[1][0] - m3[1][0];
    double d11 = m2[1][1] - m3[1][1];
    double d12 = m2[1][2] - m3[1][2];
    double d20 = m2[2][0] - m3[2][0];
    double d21 = m2[2][1] - m3[2][1];
    double d22 = m2[2][2] - m3[2][2];

    Assert.assertTrue(FastMath.abs(d00) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d01) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d02) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d10) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d11) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d12) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d20) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d21) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d22) < 6.0e-6);

    Assert.assertTrue(FastMath.abs(d00) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d01) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d02) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d10) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d11) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d12) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d20) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d21) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d22) > 4.0e-7);

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        double m3tm3 = m3[i][0] * m3[j][0]
                     + m3[i][1] * m3[j][1]
                     + m3[i][2] * m3[j][2];
        if (i == j) {
          Assert.assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
        } else {
          Assert.assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
        }
      }
    }

    checkVector(r.applyTo(Vector3D.PLUS_I),
                new Vector3D(m3[0][0], m3[1][0], m3[2][0]));
    checkVector(r.applyTo(Vector3D.PLUS_J),
                new Vector3D(m3[0][1], m3[1][1], m3[2][1]));
    checkVector(r.applyTo(Vector3D.PLUS_K),
                new Vector3D(m3[0][2], m3[1][2], m3[2][2]));

    double[][] m4 = { { 1.0,  0.0,  0.0 },
                      { 0.0, -1.0,  0.0 },
                      { 0.0,  0.0, -1.0 } };
    r = new Rotation(m4, 1.0e-7);
    checkAngle(r.getAngle(), FastMath.PI);

    try {
      double[][] m5 = { { 0.0, 0.0, 1.0 },
                        { 0.0, 1.0, 0.0 },
                        { 1.0, 0.0, 0.0 } };
      r = new Rotation(m5, 1.0e-7);
      Assert.fail("got " + r + ", should have caught an exception");
    } catch (NotARotationMatrixException e) {
      
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testAngles
  public void testAngles()
    throws CardanEulerSingularityException {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    for (int i = 0; i < CardanOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(CardanOrders[i], alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(CardanOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    for (int i = 0; i < EulerOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(EulerOrders[i],
                                      alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(EulerOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testSingularities
  public void testSingularities() {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    double[] singularCardanAngle = { FastMath.PI / 2, -FastMath.PI / 2 };
    for (int i = 0; i < CardanOrders.length; ++i) {
      for (int j = 0; j < singularCardanAngle.length; ++j) {
        Rotation r = new Rotation(CardanOrders[i], 0.1, singularCardanAngle[j], 0.3);
        try {
          r.getAngles(CardanOrders[i]);
          Assert.fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    double[] singularEulerAngle = { 0, FastMath.PI };
    for (int i = 0; i < EulerOrders.length; ++i) {
      for (int j = 0; j < singularEulerAngle.length; ++j) {
        Rotation r = new Rotation(EulerOrders[i], 0.1, singularEulerAngle[j], 0.3);
        try {
          r.getAngles(EulerOrders[i]);
          Assert.fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testQuaternion
  public void testQuaternion() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    double n = 23.5;
    Rotation r2 = new Rotation(n * r1.getQ0(), n * r1.getQ1(),
                               n * r1.getQ2(), n * r1.getQ3(),
                               true);
    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(u), r1.applyTo(u));
        }
      }
    }

    r1 = new Rotation( 0.288,  0.384,  0.36,  0.8, false);
    checkRotation(r1, -r1.getQ0(), -r1.getQ1(), -r1.getQ2(), -r1.getQ3());

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testCompose
  public void testCompose() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testComposeInverse
  public void testComposeInverse() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyInverseTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testApplyInverseTo
  public void testApplyInverseTo() {

    Rotation r = new Rotation(new Vector3D(2, -3, 5), 1.7);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          r.applyInverseTo(r.applyTo(u));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = Rotation.IDENTITY;
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = new Rotation(Vector3D.PLUS_K, FastMath.PI);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

  }

// org.apache.commons.math.geometry.euclidean.threed.RotationTest::testIssue639
  public void testIssue639(){
      Vector3D u1 = new Vector3D(-1321008684645961.0 /  268435456.0,
                                 -5774608829631843.0 /  268435456.0,
                                 -3822921525525679.0 / 4294967296.0);
      Vector3D u2 =new Vector3D( -5712344449280879.0 /    2097152.0,
                                 -2275058564560979.0 /    1048576.0,
                                  4423475992255071.0 /      65536.0);
      Rotation rot = new Rotation(u1, u2, Vector3D.PLUS_I,Vector3D.PLUS_K);
      Assert.assertEquals( 0.6228370359608200639829222, rot.getQ0(), 1.0e-15);
      Assert.assertEquals( 0.0257707621456498790029987, rot.getQ1(), 1.0e-15);
      Assert.assertEquals(-0.0000000002503012255839931, rot.getQ2(), 1.0e-15);
      Assert.assertEquals(-0.7819270390861109450724902, rot.getQ3(), 1.0e-15);
  }
