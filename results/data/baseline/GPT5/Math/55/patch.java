public static Vector3D crossProduct(final Vector3D v1, final Vector3D v2) {

      // rescale both vectors to reduce overflow/underflow, then scale back
      final double m1 = Math.max(Math.max(Math.abs(v1.x), Math.abs(v1.y)), Math.abs(v1.z));
      final double m2 = Math.max(Math.max(Math.abs(v2.x), Math.abs(v2.y)), Math.abs(v2.z));
      final double s = Math.max(m1, m2);
      if (s == 0.0) {
          return new Vector3D(0.0, 0.0, 0.0);
      }

      final double a1x = v1.x / s;
      final double a1y = v1.y / s;
      final double a1z = v1.z / s;
      final double a2x = v2.x / s;
      final double a2y = v2.y / s;
      final double a2z = v2.z / s;

      final double cx = a1y * a2z - a1z * a2y;
      final double cy = a1z * a2x - a1x * a2z;
      final double cz = a1x * a2y - a1y * a2x;

      final double scale = s * s;
      return new Vector3D(cx * scale, cy * scale, cz * scale);

  }