// org/apache/commons/math/geometry/euclidean/threed/RotationTest.java
@Test
  public void testRotationAxisAngle2() {
      Vector3D axis = new Vector3D(1, -2, -1);
      double angle = Math.PI / 4;
      Rotation r1 = new Rotation(axis, angle);
      Vector3D u1 = Vector3D.PLUS_I;
      Vector3D u2 = Vector3D.PLUS_J;
      Vector3D v1 = r1.applyTo(u1);
      Vector3D v2 = r1.applyTo(u2);
      Rotation r2 = new Rotation(u1, u2, v1, v2);
      Vector3D test = new Vector3D(2, -1, 5);
      Vector3D rotated1 = r1.applyTo(test);
      Vector3D rotated2 = r2.applyTo(test);
      Assert.assertEquals(rotated1.getX(), rotated2.getX(), 1.0e-15);
      Assert.assertEquals(rotated1.getY(), rotated2.getY(), 1.0e-15);
      Assert.assertEquals(rotated1.getZ(), rotated2.getZ(), 1.0e-15);
  }
