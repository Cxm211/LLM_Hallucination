public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
        super(populationLimit);
        if (elitismRate < 0 || elitismRate > 1) {
            throw new org.apache.commons.math3.exception.OutOfRangeException(elitismRate, 0, 1);
        }
        this.elitismRate = elitismRate;
    }