    public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
        super(populationLimit);
        if (Double.isNaN(elitismRate) || elitismRate < 0 || elitismRate > 1) {
            throw new OutOfRangeException(elitismRate, 0, 1);
        }
        this.elitismRate = elitismRate;
    }