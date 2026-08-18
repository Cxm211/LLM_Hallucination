public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
        super(chromosomes, populationLimit);
        if (elitismRate < 0 || elitismRate > 1) {
            throw new IllegalArgumentException("Elitism rate must be in [0,1]");
        }
        this.elitismRate = elitismRate;
    }