public double[] repairAndDecode(final double[] x) {
    if (x == null) {
        return null;
    }
    return decode(x.clone());
}