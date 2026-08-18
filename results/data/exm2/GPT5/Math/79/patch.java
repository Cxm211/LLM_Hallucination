public static double distance(int[] p1, int[] p2) {
  double sum = 0.0;
  for (int i = 0; i < p1.length; i++) {
      final double dp = (double) p1[i] - (double) p2[i];
      sum += dp * dp;
  }
  return Math.sqrt(sum);
}