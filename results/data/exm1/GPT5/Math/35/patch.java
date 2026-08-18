public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
        super(chromosomes, populationLimit);
        if (elitismRate < 0 || elitismRate > 1) {
            throw new org.apache.commons.math3.exception.OutOfRangeException(elitismRate, 0, 1);
        }
        this.elitismRate = elitismRate;
    }