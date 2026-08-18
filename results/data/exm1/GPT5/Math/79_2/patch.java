public static double distance(int[] p1, int[] p2) {
      long sum = 0L;
      for (int i = 0; i < p1.length; i++) {
          final long dp = ((long) p1[i]) - ((long) p2[i]);
          sum += dp * dp;
      }
      return Math.sqrt((double) sum);
    }