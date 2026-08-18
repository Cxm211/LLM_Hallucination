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

      // rescale to avoid infinite intermediate values
      double max = FastMath.max(FastMath.max(FastMath.abs(v1.x), FastMath.abs(v1.y)),
                                FastMath.max(FastMath.abs(v1.z), FastMath.max(FastMath.abs(v2.x),
                                                                              FastMath.max(FastMath.abs(v2.y), FastMath.abs(v2.z)))));
      if (max == 0.0) {
          return new Vector3D(0, 0, 0);
      }

      // scale factor as a power of two to avoid precision loss
      final int exponent = FastMath.getExponent(max);
      final double scale = FastMath.scalb(1.0, -exponent);
      final double invScale = 1.0 / scale;

      final double v1x = v1.x * scale;
      final double v1y = v1.y * scale;
      final double v1z = v1.z * scale;
      final double v2x = v2.x * scale;
      final double v2y = v2.y * scale;
      final double v2z = v2.z * scale;

      final double dot12 = v1x * v2x + v1y * v2y + v1z * v2z;
      final double dot22 = v2x * v2x + v2y * v2y + v2z * v2z;
      if (dot22 == 0.0) {
          return new Vector3D(0, 0, 0);
      }

      double rho = dot12 / dot22;
      // round to 8 bits
      rho = FastMath.rint(rho * 256.0) / 256.0;

      final double v3x = v1x - rho * v2x;
      final double v3y = v1y - rho * v2y;
      final double v3z = v1z - rho * v2z;

      final double cx = v3y * v2z - v3z * v2y;
      final double cy = v3z * v2x - v3x * v2z;
      final double cz = v3x * v2y - v3y * v2x;

      // rescale back
      final double rescale = invScale * invScale; // 1/(scale*scale)
      return new Vector3D(cx * rescale, cy * rescale, cz * rescale);

  }