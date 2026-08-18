public void addValue(double value) {
        sumImpl.increment(value);
        sumsqImpl.increment(value);
        minImpl.increment(value);
        maxImpl.increment(value);
        sumLogImpl.increment(value);
        secondMoment.increment(value);
        // If mean, variance or geomean have been overridden,
        // need to increment these
        if (meanImpl.getClass() != Mean.class) {
            meanImpl.increment(value);
        }
        if (varianceImpl.getClass() != Variance.class) {
            varianceImpl.increment(value);
        }
        if (geoMeanImpl.getClass() != GeometricMean.class) {
            geoMeanImpl.increment(value);
        }
        n++;
    }