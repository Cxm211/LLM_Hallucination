public static double distance(int[] p1, int[] p2) {
      if (p1 == null || p2 == null) {
          throw new NullPointerException();
      }
      if (p1.length != p2.length) {
          throw new IllegalArgumentException("Point dimensions must match");
      }
      double sum = 0.0;
      for (int i = 0; i < p1.length; i++) {
          long dp = (long) p1[i] - (long) p2[i];
          sum += (double) dp * (double) dp;
      }
      return Math.sqrt(sum);
    }