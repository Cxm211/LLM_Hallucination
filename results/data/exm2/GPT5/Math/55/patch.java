public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {
    final double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    final double norm2 = v2.x * v2.x + v2.y * v2.y + v2.z * v2.z;
    final double rho = Math.rint((dot / norm2) * 256.0) / 256.0;
    final double x3 = v1.x - rho * v2.x;
    final double y3 = v1.y - rho * v2.y;
    final double z3 = v1.z - rho * v2.z;
    return new Vector3D(y3 * v2.z - z3 * v2.y, z3 * v2.x - x3 * v2.z, x3 * v2.y - y3 * v2.x);
}