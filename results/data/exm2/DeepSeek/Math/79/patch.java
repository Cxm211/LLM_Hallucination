    public static double distance(int[] p1, int[] p2) {
      long sum = 0L;
      for (int i = 0; i < p1.length; i++) {
          long diff = (long) p1[i] - (long) p2[i];
          sum += diff * diff;
      }
      return Math.sqrt(sum);
    }