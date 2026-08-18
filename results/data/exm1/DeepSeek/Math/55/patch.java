public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {


      // rescale both vectors without losing precision,
      // to ensure their norm are the same order of magnitude

      // we reduce cancellation errors by preconditioning,
      // we replace v1 by v3 = v1 - rho v2 with rho chosen in order to compute
      // v3 without loss of precision. See Kahan lecture
      // "Computing Cross-Products and Rotations in 2- and 3-Dimensional Euclidean Spaces"
      // available at http://www.cs.berkeley.edu/~wkahan/MathH110/Cross.pdf

      // compute rho as an 8 bits approximation of v1.v2 / v2.v2
      double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
      double normSq = v2.x * v2.x + v2.y * v2.y + v2.z * v2.z;
      double rho = Math.rint(dot / normSq * 256) / 256;

      // compute v3 = v1 - rho * v2
      double c1 = v1.x - rho * v2.x;
      double c2 = v1.y - rho * v2.y;
      double c3 = v1.z - rho * v2.z;

      // compute cross product from v3 and v2 instead of v1 and v2
      return new Vector3D(c2 * v2.z - c3 * v2.y, c3 * v2.x - c1 * v2.z, c1 * v2.y - c2 * v2.x);

  }