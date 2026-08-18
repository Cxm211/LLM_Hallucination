public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
        super(populationLimit);
        if (elitismRate < 0 || elitismRate > 1) {
            throw new IllegalArgumentException("Elitism rate must be in [0,1]");
        }
        this.elitismRate = elitismRate;
    }