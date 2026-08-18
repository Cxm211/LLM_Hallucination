// buggy code
    public Iterator<Chromosome> iterator() {
        return chromosomes.iterator();
    }

// relevant test
// org.apache.commons.math3.genetics.ChromosomeTest::testCompareTo
    public void testCompareTo() {
        Chromosome c1 = new Chromosome() {
            public double fitness() {
                return 0;
            }
        };
        Chromosome c2 = new Chromosome() {
            public double fitness() {
                return 10;
            }
        };
        Chromosome c3 = new Chromosome() {
            public double fitness() {
                return 10;
            }
        };

        Assert.assertTrue(c1.compareTo(c2) < 0);
        Assert.assertTrue(c2.compareTo(c1) > 0);
        Assert.assertEquals(0,c3.compareTo(c2));
        Assert.assertEquals(0,c2.compareTo(c3));
    }

// org.apache.commons.math3.genetics.ChromosomeTest::testFindSameChromosome
    public void testFindSameChromosome() {
        Chromosome c1 = new DummyChromosome(1) {
            public double fitness() {
                return 1;
            }
        };
        Chromosome c2 = new DummyChromosome(2) {
            public double fitness() {
                return 2;
            }
        };
        Chromosome c3 = new DummyChromosome(3) {
            public double fitness() {
                return 3;
            }
        };
        Chromosome c4 = new DummyChromosome(1) {
            public double fitness() {
                return 5;
            }
        };
        Chromosome c5 = new DummyChromosome(15) {
            public double fitness() {
                return 15;
            }
        };

        List<Chromosome> popChr = new ArrayList<Chromosome>();
        popChr.add(c1);
        popChr.add(c2);
        popChr.add(c3);
        Population pop = new ListPopulation(popChr,3) {
            public Population nextGeneration() {
                
                return null;
            }
        };

        Assert.assertNull(c5.findSameChromosome(pop));
        Assert.assertEquals(c1, c4.findSameChromosome(pop));

        c4.searchForFitnessUpdate(pop);
        Assert.assertEquals(1, c4.getFitness(),0);
    }

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

// org.apache.commons.math3.genetics.ListPopulationTest::testGetFittestChromosome
    public void testGetFittestChromosome() {
        Chromosome c1 = new Chromosome() {
            public double fitness() {
                return 0;
            }
        };
        Chromosome c2 = new Chromosome() {
            public double fitness() {
                return 10;
            }
        };
        Chromosome c3 = new Chromosome() {
            public double fitness() {
                return 15;
            }
        };

        ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(c1);
        chromosomes.add(c2);
        chromosomes.add(c3);

        ListPopulation population = new ListPopulation(chromosomes, 10) {
            public Population nextGeneration() {
                
                return null;
            }
        };

        Assert.assertEquals(c3, population.getFittestChromosome());
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testChromosomes
    public void testChromosomes() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));        

        final ListPopulation population = new ListPopulation(10) {
            public Population nextGeneration() {
                
                return null;
            }
        };
        
        population.addChromosomes(chromosomes);

        Assert.assertEquals(chromosomes, population.getChromosomes());
        Assert.assertEquals(chromosomes.toString(), population.toString());
        
        population.setPopulationLimit(50);
        Assert.assertEquals(50, population.getPopulationLimit());
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testSetPopulationLimit
    public void testSetPopulationLimit() {
        final ListPopulation population = new ListPopulation(10) {
            public Population nextGeneration() {
                
                return null;
            }
        };
        
        population.setPopulationLimit(-50);
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testConstructorPopulationLimitNotPositive
    public void testConstructorPopulationLimitNotPositive() {
        new ListPopulation(-10) {
            public Population nextGeneration() {
                
                return null;
            }
        };
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testChromosomeListConstructorPopulationLimitNotPositive
    public void testChromosomeListConstructorPopulationLimitNotPositive() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        new ListPopulation(chromosomes, -10) {
            public Population nextGeneration() {
                
                return null;
            }
        };
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testConstructorListOfChromosomesBiggerThanPopulationSize
    public void testConstructorListOfChromosomesBiggerThanPopulationSize() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));        
        new ListPopulation(chromosomes, 1) {
            public Population nextGeneration() {
                
                return null;
            }
        };
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testAddTooManyChromosomes
    public void testAddTooManyChromosomes() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));

        final ListPopulation population = new ListPopulation(2) {
            public Population nextGeneration() {
                
                return null;
            }
        };
        
        population.addChromosomes(chromosomes);
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testAddTooManyChromosomesSingleCall
    public void testAddTooManyChromosomesSingleCall() {

        final ListPopulation population = new ListPopulation(2) {
            public Population nextGeneration() {
                
                return null;
            }
        };

        for (int i = 0; i <= population.getPopulationLimit(); i++) {
            population.addChromosome(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        }
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testIterator
    public void testIterator() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));

        final ListPopulation population = new ListPopulation(10) {
            public Population nextGeneration() {
                
                return null;
            }
        };

        population.addChromosomes(chromosomes);

        final Iterator<Chromosome> iter = population.iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }

// org.apache.commons.math3.genetics.ListPopulationTest::testSetPopulationLimitTooSmall
    public void testSetPopulationLimitTooSmall() {
        final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome> ();
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
        chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));

        final ListPopulation population = new ListPopulation(chromosomes, 3) {
            public Population nextGeneration() {
                
                return null;
            }
        };

        population.setPopulationLimit(2);
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
