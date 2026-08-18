public static double distance(int[] p1, int[] p2) {
      if (p1 == null || p2 == null) {
          throw new IllegalArgumentException("points must not be null");
      }
      if (p1.length != p2.length) {
          throw new IllegalArgumentException("points must have same dimension");
      }
      long sum = 0L;
      for (int i = 0; i < p1.length; i++) {
          long dp = (long) p1[i] - (long) p2[i];
          sum += dp * dp;
      }
      return Math.sqrt((double) sum);
    }