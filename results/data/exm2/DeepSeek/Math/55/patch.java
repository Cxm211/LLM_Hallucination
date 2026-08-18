  public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {

      // rescale both vectors without losing precision,
      // to ensure their norm are the same order of magnitude

      // we reduce cancellation errors by preconditioning,
      // we replace v1 by v3 = v1 - rho v2 with rho chosen in order to compute
      // v3 without loss of precision. See Kahan lecture
      // "Computing Cross-Products and Rotations in 2- and 3-Dimensional Euclidean Spaces"
      // available at http://www.cs.berkeley.edu/~wkahan/MathH110/Cross.pdf

      // compute rho as an 8 bits approximation of v1.v2 / v2.v2

      // First, check if v2 is zero to avoid division by zero
      if (v2.x == 0 && v2.y == 0 && v2.z == 0) {
          return new Vector3D(0, 0, 0);
      }

      // Compute dot products
      double dot12 = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
      double dot22 = v2.x * v2.x + v2.y * v2.y + v2.z * v2.z;

      // Compute rho
      double rho = dot12 / dot22;

      // Compute v3
      double v3x = v1.x - rho * v2.x;
      double v3y = v1.y - rho * v2.y;
      double v3z = v1.z - rho * v2.z;

      // compute cross product from v3 and v2 instead of v1 and v2
      return new Vector3D(v3y * v2.z - v3z * v2.y, v3z * v2.x - v3x * v2.z, v3x * v2.y - v3y * v2.x);

  }