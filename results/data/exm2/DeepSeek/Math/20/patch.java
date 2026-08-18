public double[] repairAndDecode(final double[] x) {
    // First, repair x to be within bounds
    final double[] repaired = x.clone();
    for (int i = 0; i < repaired.length; i++) {
        if (repaired[i] < lowerBounds[i]) {
            repaired[i] = lowerBounds[i];
        }
        if (repaired[i] > upperBounds[i]) {
            repaired[i] = upperBounds[i];
        }
    }
    // Then decode the repaired point
    return decode(repaired);
}