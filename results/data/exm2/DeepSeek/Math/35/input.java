    public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
        super(chromosomes, populationLimit);
        this.elitismRate = elitismRate;
    }

    public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
        super(populationLimit);
        this.elitismRate = elitismRate;
    }

// trigger testcase
public void testChromosomeListConstructorTooHigh() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = 1.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

public void testChromosomeListConstructorTooLow() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = -0.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

public void testConstructorTooHigh() {
        final double rate = 1.25;
        new ElitisticListPopulation(100, rate);
    }

public void testConstructorTooLow() {
        final double rate = -0.25;
        new ElitisticListPopulation(100, rate);
    }
