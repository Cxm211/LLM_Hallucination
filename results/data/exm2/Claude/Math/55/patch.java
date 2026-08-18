  public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {


      // rescale both vectors without losing precision,
      // to ensure their norm are the same order of magnitude

      // we reduce cancellation errors by preconditioning,
      // we replace v1 by v3 = v1 - rho v2 with rho chosen in order to compute
      // v3 without loss of precision. See Kahan lecture
      // "Computing Cross-Products and Rotations in 2- and 3-Dimensional Euclidean Spaces"
      // available at http://www.cs.berkeley.edu/~wkahan/MathH110/Cross.pdf

      // compute rho as an 8 bits approximation of v1.v2 / v2.v2

      final double nv1 = v1.getNorm();
      final double nv2 = v2.getNorm();

      if ((nv1 == 0) || (nv2 == 0)) {
          return ZERO;
      }

      final double threshold = FastMath.max(nv1, nv2) * FastMath.ulp(1.0);
      if (FastMath.abs(nv1 - nv2) <= threshold) {
          final double dot = v1.dotProduct(v2);
          final double n = nv1 * nv2;
          if (FastMath.abs(dot) >= n * 0.9999) {
              final Vector3D v3;
              if (FastMath.abs(v1.x) <= threshold) {
                  v3 = new Vector3D(0, v1.y, v1.z);
              } else if (FastMath.abs(v1.y) <= threshold) {
                  v3 = new Vector3D(v1.x, 0, v1.z);
              } else {
                  v3 = new Vector3D(v1.x, v1.y, 0);
              }
              final double dot2   = v2.dotProduct(v2);
              final double rho    = dot / dot2;
              final double v3x    = v1.x - rho * v2.x;
              final double v3y    = v1.y - rho * v2.y;
              final double v3z    = v1.z - rho * v2.z;
              return new Vector3D(v3y * v2.z - v3z * v2.y,
                                  v3z * v2.x - v3x * v2.z,
                                  v3x * v2.y - v3y * v2.x);
          }
      }

      // compute cross product from v3 and v2 instead of v1 and v2
      return new Vector3D(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);

  }