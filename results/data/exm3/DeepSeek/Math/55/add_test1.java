// org/apache/commons/math/geometry/Vector3DTest.java
@Test
    public void testCrossProductCancellation3() {
        double x1 = -10000000001.0;
        double y1 = 20000000002.0;
        double z1 = 30000000003.0;
        double x2 = -10000000002.0;
        double y2 = 20000000001.0;
        double z2 = 30000000004.0;
        Vector3D v1 = new Vector3D(x1, y1, z1);
        Vector3D v2 = new Vector3D(x2, y2, z2);
        // Compute exact cross product using integer arithmetic
        long lx1 = (long) x1;
        long ly1 = (long) y1;
        long lz1 = (long) z1;
        long lx2 = (long) x2;
        long ly2 = (long) y2;
        long lz2 = (long) z2;
        long cx = ly1 * lz2 - lz1 * ly2;
        long cy = lz1 * lx2 - lx1 * lz2;
        long cz = lx1 * ly2 - ly1 * lx2;
        double expectedX = (double) cx;
        double expectedY = (double) cy;
        double expectedZ = (double) cz;
        checkVector(Vector3D.crossProduct(v1, v2), expectedX, expectedY, expectedZ);
    }
