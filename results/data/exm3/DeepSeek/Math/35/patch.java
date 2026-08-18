    public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
        super(chromosomes, populationLimit);
        if (Double.isNaN(elitismRate) || elitismRate < 0 || elitismRate > 1) {
            throw new OutOfRangeException(elitismRate, 0, 1);
        }
        this.elitismRate = elitismRate;
    }