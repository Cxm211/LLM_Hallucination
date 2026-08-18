// org/apache/commons/math/geometry/Vector3DTest.java
@Test
    public void testCrossProductOrthogonal() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
        Vector3D result = Vector3D.crossProduct(v1, v2);
        checkVector(result, 0, 0, 1);
    }