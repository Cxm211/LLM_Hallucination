// ===== FIXED org.apache.commons.math.optimization.fitting.GaussianFitter :: fit() [lines 119-122] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-58-fixed/src/main/java/org/apache/commons/math/optimization/fitting/GaussianFitter.java =====
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(guess);
    }
