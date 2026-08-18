// ===== FIXED org.apache.commons.math.util.MathUtils :: distance(int[], int[]) [lines 1623-1630] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-79-fixed/src/main/java/org/apache/commons/math/util/MathUtils.java =====
    public static double distance(int[] p1, int[] p2) {
      double sum = 0;
      for (int i = 0; i < p1.length; i++) {
          final double dp = p1[i] - p2[i];
          sum += dp * dp;
      }
      return Math.sqrt(sum);
    }
