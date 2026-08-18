public double[] decode(final double[] x) {
    if (boundaries == null) {
        return x;
    }
    double[] res = new double[x.length];
    for (int i = 0; i < x.length; i++) {
        double diff = boundaries[1][i] - boundaries[0][i];
        if (diff == 0) {
            res[i] = boundaries[0][i];
        } else {
            res[i] = diff * x[i] + boundaries[0][i];
        }
    }
    return res;
}