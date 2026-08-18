// org/apache/commons/math/geometry/euclidean/threed/RotationTest.java
@Test
  public void testRotationConstructorBranch2() {
    Vector3D u1 = new Vector3D(1.0, 0.0, 0.0);
    Vector3D u2 = new Vector3D(0.0, 1.0, 0.0);
    Vector3D v1 = new Vector3D(0.0, 1.0, 0.0);
    Vector3D v2 = new Vector3D(-1.0, 0.0, 0.0);
    Rotation rot = new Rotation(u1, u2, v1, v2);
    Assert.assertTrue(Math.abs(rot.getQ0() * rot.getQ0() + rot.getQ1() * rot.getQ1() + rot.getQ2() * rot.getQ2() + rot.getQ3() * rot.getQ3() - 1.0) < 1.0e-15);
    Vector3D resultU1 = rot.applyTo(u1);
    Vector3D resultU2 = rot.applyTo(u2);
    Assert.assertEquals(0.0, resultU1.distance(v1), 1.0e-15);
    Assert.assertEquals(0.0, resultU2.distance(v2), 1.0e-15);
  }