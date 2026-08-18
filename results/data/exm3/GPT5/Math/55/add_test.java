// org/apache/commons/math/geometry/Vector3DTest.java::testCrossProductOverflowPrevention
@Test
public void testCrossProductOverflowPrevention() {
    double t = FastMath.scalb(1.0, 500);
    Vector3D v1 = new Vector3D(9070467121.0 * t, 4535233560.0 * t, 1.0 * t);
    Vector3D v2 = new Vector3D(9070467123.0 * t, 4535233561.0 * t, 1.0 * t);
    Vector3D c = Vector3D.crossProduct(v1, v2);
    double t2 = t * t;
    checkVector(c, -t2, 2 * t2, t2);
}