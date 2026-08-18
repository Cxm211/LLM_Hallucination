// buggy code
  public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {


      // rescale both vectors without losing precision,
      // to ensure their norm are the same order of magnitude

      // we reduce cancellation errors by preconditioning,
      // we replace v1 by v3 = v1 - rho v2 with rho chosen in order to compute
      // v3 without loss of precision. See Kahan lecture
      // "Computing Cross-Products and Rotations in 2- and 3-Dimensional Euclidean Spaces"
      // available at http://www.cs.berkeley.edu/~wkahan/MathH110/Cross.pdf

      // compute rho as an 8 bits approximation of v1.v2 / v2.v2


      // compute cross product from v3 and v2 instead of v1 and v2
      // Implement Kahan-preconditioned cross product to reduce cancellation
      final double x1 = v1.x;
      final double y1 = v1.y;
      final double z1 = v1.z;
      final double x2 = v2.x;
      final double y2 = v2.y;
      final double z2 = v2.z;

      final double s1 = FastMath.max(FastMath.max(FastMath.abs(x1), FastMath.abs(y1)), FastMath.abs(z1));
      final double s2 = FastMath.max(FastMath.max(FastMath.abs(x2), FastMath.abs(y2)), FastMath.abs(z2));

      if (s1 == 0.0 || s2 == 0.0) {
          return new Vector3D(0.0, 0.0, 0.0);
      }

      final double x1s = x1 / s1;
      final double y1s = y1 / s1;
      final double z1s = z1 / s1;
      final double x2s = x2 / s2;
      final double y2s = y2 / s2;
      final double z2s = z2 / s2;

      final double ratioScaled = (x1s * x2s + y1s * y2s + z1s * z2s) /
                                 (x2s * x2s + y2s * y2s + z2s * z2s);
      final double ratio = ratioScaled * (s1 / s2);

      final double rho = FastMath.scalb(FastMath.rint(FastMath.scalb(ratio, 8)), -8);

      final double v3x = x1 - rho * x2;
      final double v3y = y1 - rho * y2;
      final double v3z = z1 - rho * z2;

      return new Vector3D(v3y * z2 - v3z * y2,
                          v3z * x2 - v3x * z2,
                          v3x * y2 - v3y * x2);

  }