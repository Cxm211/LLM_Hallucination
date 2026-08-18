    private double calculateAsymptoticPValue(final double Umin,
                                             final int n1,
                                             final int n2)
        throws ConvergenceException, MaxCountExceededException {

        final double n1d = n1;
        final double n2d = n2;
        final double n1n2prod = n1d * n2d;

        // http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U#Normal_approximation
        final double EU = n1n2prod / 2.0;
        final double VarU = n1n2prod * (n1d + n2d + 1.0) / 12.0;

        final double z = (Umin - EU) / FastMath.sqrt(VarU);

        final NormalDistribution standardNormal = new NormalDistribution(0, 1);

        return 2 * standardNormal.cumulativeProbability(z);
    }