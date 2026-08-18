public boolean isFeasible(final double[] x) {
    if (boundaries == null) {
        return true;
    }

    for (int i = 0; i < x.length; i++) {
        double encoded = (x[i] - boundaries[0][i]) / (boundaries[1][i] - boundaries[0][i]);
        if (encoded < 0) {
            return false;
        }
        if (encoded > 1.0) {
            return false;
        }
    }
    return true;
}