// org/apache/commons/math/geometry/euclidean/threed/RotationTest.java
@Test
  public void testRotationConstructorBranch3() {
    Vector3D u1 = new Vector3D(1.0, 1.0, 1.0);
    Vector3D u2 = new Vector3D(2.0, 3.0, 4.0);
    Vector3D v1 = new Vector3D(1.0, 1.0, 1.0);
    Vector3D v2 = new Vector3D(2.0, 3.0, 4.0);
    Rotation rot = new Rotation(u1, u2, v1, v2);
    Assert.assertEquals(1.0, rot.getQ0(), 1.0e-15);
    Assert.assertEquals(0.0, rot.getQ1(), 1.0e-15);
    Assert.assertEquals(0.0, rot.getQ2(), 1.0e-15);
    Assert.assertEquals(0.0, rot.getQ3(), 1.0e-15);
  }