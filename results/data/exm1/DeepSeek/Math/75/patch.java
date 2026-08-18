    public double getPct(Object v) {
        return getCount((Comparable<?>) v) / (double) getSumFreq();
    }