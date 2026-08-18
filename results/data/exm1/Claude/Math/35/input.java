// buggy code
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

// relevant test
// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testNextGeneration
    public void testNextGeneration() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);

        for (int i=0; i<pop.getPopulationLimit(); i++) {
            pop.addChromosome(new DummyChromosome());
        }

        Population nextGeneration = pop.nextGeneration();

        Assert.assertEquals(20, nextGeneration.getPopulationSize());
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRate
    public void testSetElitismRate() {
        final double rate = 0.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
        Assert.assertEquals(rate, pop.getElitismRate(), 1e-6);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRateTooLow
    public void testSetElitismRateTooLow() {
        final double rate = -0.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRateTooHigh
    public void testSetElitismRateTooHigh() {
        final double rate = 1.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooLow
    public void testConstructorTooLow() {
        final double rate = -0.25;
        new ElitisticListPopulation(100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh
    public void testConstructorTooHigh() {
        final double rate = 1.25;
        new ElitisticListPopulation(100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow
    public void testChromosomeListConstructorTooLow() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = -0.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooHigh
    public void testChromosomeListConstructorTooHigh() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = 1.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

// org.apache.commons.math3.genetics.FitnessCachingTest::testFitnessCaching
    public void testFitnessCaching() {
        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE, 
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        ga.evolve(initial, stopCond);

        int neededCalls =
            POPULATION_SIZE  +
            (NUM_GENERATIONS - 1)  * (int)(POPULATION_SIZE * (1.0 - ELITISM_RATE)) 
            ;
        Assert.assertTrue(fitnessCalls <= neededCalls); 
    }

// org.apache.commons.math3.genetics.GeneticAlgorithmTestBinary::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        Assert.assertEquals(0, ga.getGenerationsEvolved());

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);
        Assert.assertEquals(NUM_GENERATIONS, ga.getGenerationsEvolved());

    }

// org.apache.commons.math3.genetics.GeneticAlgorithmTestPermutations::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE,
                new RandomKeyMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);

        
        
    }

// org.apache.commons.math3.genetics.TournamentSelectionTest::testSelect
    public void testSelect() {
        TournamentSelection ts = new TournamentSelection(2);
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);

        for (int i=0; i<pop.getPopulationLimit(); i++) {
            pop.addChromosome(new DummyChromosome());
        }
        
        for (int i=0; i<20; i++) {
            ChromosomePair pair = ts.select(pop);
            
            Assert.assertTrue(pair.getFirst().getFitness() > 0);
            Assert.assertTrue(pair.getSecond().getFitness() > 0);
        }
    }
