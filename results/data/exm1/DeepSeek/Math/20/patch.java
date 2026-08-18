public double[] repairAndDecode(final double[] x) {
    double[] repaired = x.clone();
    for (int i = 0; i < repaired.length; i++) {
        if (repaired[i] < 0.0) {
            repaired[i] = 0.0;
        } else if (repaired[i] > 1.0) {
            repaired[i] = 1.0;
        }
    }
    return decode(repaired);
}