public void addValue(double value) {
    sumImpl.increment(value);
    sumsqImpl.increment(value);
    minImpl.increment(value);
    maxImpl.increment(value);
    meanImpl.increment(value);
    varianceImpl.increment(value);
    geoMeanImpl.increment(value);
    n++;
}