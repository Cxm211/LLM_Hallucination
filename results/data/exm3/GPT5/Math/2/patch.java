public double getNumericalMean() {
        // Avoid integer overflow by promoting to double before multiplication
        return ((double) getSampleSize()) * ((double) getNumberOfSuccesses()) / ((double) getPopulationSize());
    }