// org/apache/commons/math3/genetics/ListPopulationTest.java::testIterator
public void testIteratorConcurrent() {
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

        final Iterator<Chromosome> it1 = population.iterator();
        final Iterator<Chromosome> it2 = population.iterator();

        // Remove one element using first iterator
        if (it1.hasNext()) {
            it1.next();
            it1.remove();
        }
        // Continue iterating using second iterator without triggering CME
        while (it2.hasNext()) {
            it2.next();
        }
    }