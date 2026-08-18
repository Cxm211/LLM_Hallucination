public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {

      // handle zero vectors
      if ((v1.x == 0.0 && v1.y == 0.0 && v1.z == 0.0) ||
          (v2.x == 0.0 && v2.y == 0.0 && v2.z == 0.0)) {
          return new Vector3D(0.0, 0.0, 0.0);
      }

      // extract components
      final double x1 = v1.x;
      final double y1 = v1.y;
      final double z1 = v1.z;
      final double x2 = v2.x;
      final double y2 = v2.y;
      final double z2 = v2.z;

      // scale vectors to avoid overflow/underflow in intermediate computations
      final double n1 = FastMath.max(FastMath.max(FastMath.abs(x1), FastMath.abs(y1)), FastMath.abs(z1));
      final double n2 = FastMath.max(FastMath.max(FastMath.abs(x2), FastMath.abs(y2)), FastMath.abs(z2));

      final int e1 = FastMath.getExponent(n1);
      final int e2 = FastMath.getExponent(n2);

      final double sx1 = FastMath.scalb(x1, -e1);
      final double sy1 = FastMath.scalb(y1, -e1);
      final double sz1 = FastMath.scalb(z1, -e1);

      final double sx2 = FastMath.scalb(x2, -e2);
      final double sy2 = FastMath.scalb(y2, -e2);
      final double sz2 = FastMath.scalb(z2, -e2);

      // compute an 8-bit approximation of rho = (v1.v2) / (v2.v2)
      final double dot12 = sx1 * sx2 + sy1 * sy2 + sz1 * sz2;
      final double dot22 = sx2 * sx2 + sy2 * sy2 + sz2 * sz2;

      if (dot22 == 0.0) {
          return new Vector3D(0.0, 0.0, 0.0);
      }

      double rho = dot12 / dot22;
      long bits = Double.doubleToLongBits(rho);
      long signExp = bits & 0xfff0000000000000L;
      long mant = bits & 0x000fffffffffffffL;
      mant &= 0x0000ff0000000000L; // keep only top 8 bits of mantissa
      final double rhoA = Double.longBitsToDouble(signExp | mant);

      // form v3 = v1 - rhoA * v2 using scaled vectors
      final double v3x = sx1 - rhoA * sx2;
      final double v3y = sy1 - rhoA * sy2;
      final double v3z = sz1 - rhoA * sz2;

      // compute cross product from v3 and scaled v2
      final double cx = v3y * sz2 - v3z * sy2;
      final double cy = v3z * sx2 - v3x * sz2;
      final double cz = v3x * sy2 - v3y * sx2;

      // rescale result back
      final int scaleBack = e1 + e2;
      return new Vector3D(FastMath.scalb(cx, scaleBack),
                          FastMath.scalb(cy, scaleBack),
                          FastMath.scalb(cz, scaleBack));

  }